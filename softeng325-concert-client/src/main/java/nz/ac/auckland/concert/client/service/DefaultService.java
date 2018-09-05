package nz.ac.auckland.concert.client.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class DefaultService implements ConcertService {


    /*** MACROS ***/


    /* URIs */
    private static final String WEB_SERVICE_URI = "http://localhost:10000/services";
    private static final String CONCERT_SERVICE = WEB_SERVICE_URI + "/concerts";
    private static final String PERFORMER_SERVICE = WEB_SERVICE_URI + "/performers";
    private static final String USER_SERVICE = WEB_SERVICE_URI + "/users";
    private static final String AUTH_SERVICE = USER_SERVICE + "/auth";
    private static final String RESERVATION_SERVICE = WEB_SERVICE_URI + "/reservations";

    /* AWS */

    // AWS S3 access credentials for performer images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // Download directory - a directory named "images" in the user's home
    // directory.
    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String USER_DIRECTORY = System
            .getProperty("user.home");
    private static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";


    /*** FIELDS ***/


    // Logging
    private static Logger _logger = LoggerFactory
            .getLogger(DefaultService.class);

    // auth token
    private Cookie _authToken;


    /*** FUNCTIONS ***/


    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Response response;
        Client client = ClientBuilder.newClient();

        try {
            Builder builder = client.target(CONCERT_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making concerts get request...");
            response = builder.get();

            Set<ConcertDTO> concertDTOS = response.readEntity(new GenericType<Set<ConcertDTO>>() {
            });

            _logger.debug("Successfully retrieved and unmarshalled concerts from server.");
            return concertDTOS;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(PERFORMER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making performers get request...");
            response = builder.get();

            Set<PerformerDTO> performerDTOList = response.readEntity(new GenericType<Set<PerformerDTO>>() {
            });

            _logger.debug("Successfully retrieved and unmarshalled performers from server.");
            return performerDTOList;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(USER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making create user request");

            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _authToken = (Cookie) response.getCookies().values().toArray()[0];
            _logger.debug("Auth token set to: " + _authToken);

            _logger.debug("User Successfully created at url: " + response.getLocation());
            return new UserDTO(newUser.getUsername(), newUser.getPassword(),
                    newUser.getLastname(), newUser.getFirstname());
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        }finally {
            client.close();
        }
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(AUTH_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making auth request");

            try {
                response = builder.put(Entity.entity(user, MediaType.APPLICATION_XML));
            } catch (Exception e) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _authToken = (Cookie) response.getCookies().values().toArray()[0];
            _logger.debug("Auth token set to: " + _authToken);

            _logger.debug("User successfully authenticated!");
            return response.readEntity(UserDTO.class);
        } finally {
            client.close();
        }
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {

        try {
            // Get image name from performer
            String imageName = performer.getImageName();

            if (imageName == null || imageName.equals("")) {
                throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
            }

            // Create download dir
            File downloadDir = new File(DOWNLOAD_DIRECTORY);
            downloadDir.mkdir();

            File imageFile = new File(downloadDir, imageName);

            if (imageFile.exists()) {
                _logger.debug("File " + imageName + " already exists retrieving...");
                return ImageIO.read(imageFile);
            }

            _logger.debug("Establishing aws connection...");

            // Set up AWS connection
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                    AWS_ACCESS_KEY_ID,
                    AWS_SECRET_ACCESS_KEY
            );

            AmazonS3 s3 = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .withCredentials(
                            new AWSStaticCredentialsProvider(awsCredentials)
                    )
                    .build();


            _logger.debug("Downloading image " + imageName + " of artist " + performer.getName() + "...");

            // Download image
            GetObjectRequest req = new GetObjectRequest(AWS_BUCKET, imageName);
            s3.getObject(req, imageFile);

            _logger.debug("Successfully retrieved image!");
            _logger.debug("Downloaded image to: " + imageFile.getAbsolutePath());

            return ImageIO.read(imageFile);

        } catch (IOException e) {
            throw new ServiceException("Unable to read image file");
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                // TODO check images can be loaded in currently getting an image to do with invalid / corrupted jpegs
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        }
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            if (_authToken == null) {
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            }

            Builder builder = client.target(RESERVATION_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making reservation request");
            response = builder
                    .cookie("AuthToken", _authToken.getValue())
                    .post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));

            // Throw appropriate exception baseed on response
            if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            ReservationDTO reservationDTO = response.readEntity(ReservationDTO.class);
            return reservationDTO;
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        // Todo
    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        // Todo
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        return null;
    }
}
