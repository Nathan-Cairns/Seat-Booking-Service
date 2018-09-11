package nz.ac.auckland.concert.client.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.ws.rs.client.*;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.*;


public class DefaultService implements ConcertService, NewsItemService{


    /*** MACROS ***/


    /* URIs */
    private static final String WEB_SERVICE_URI = "http://localhost:10000/services";
    private static final String CONCERT_SERVICE = WEB_SERVICE_URI + "/concerts";
    private static final String PERFORMER_SERVICE = WEB_SERVICE_URI + "/performers";
    private static final String USER_SERVICE = WEB_SERVICE_URI + "/users";
    private static final String AUTH_SERVICE = USER_SERVICE + "/auth";
    private static final String BOOKINGS_SERVICE = WEB_SERVICE_URI + "/bookings";
    private static final String RESERVATION_SERVICE = BOOKINGS_SERVICE + "/reserve";
    private static final String BOOKING_CONFIRMATION_SERVICE = BOOKINGS_SERVICE + "/book";
    private static final String CREDIT_CARD_SERVICE = USER_SERVICE + "/credit_card";
    private static final String NEWS_ITEM_SERVICE = WEB_SERVICE_URI + "/news_items";
    private static final String NEWS_ITEM_SUB_SERVICE = NEWS_ITEM_SERVICE + "/sub";

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

    private Subscription _subscription = new Subscription();

    // caching
    private Set<ConcertDTO> _concertCache = new HashSet<>();
    private Set<PerformerDTO> _performerCache = new HashSet<>();
    private Set<BookingDTO> _bookingCache = new HashSet<>();

    private Date _concertCacheExpiry;
    private Date _performerCacheExpiry;
    private Date _bookingCacheExpiry;


    /*** FUNCTIONS ***/


    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        try {
            // Check cache
            if (_concertCacheExpiry == null || _concertCache == null) {
                _logger.debug("First time request, getting concerts");
                // make request and set expiry
                this.revalidateConcertCache();
            } else if (new Date().after(_concertCacheExpiry)) {
                _logger.debug("Cache out of date revalidating");
                // cache is invalidated revalidate
                this.revalidateConcertCache();
            }
            return _concertCache;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    private void revalidateConcertCache() throws ServiceException {
        _logger.debug("Update concerts cache");
        Response response;
        Client client = ClientBuilder.newClient();

        try {
            Builder builder = client.target(CONCERT_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);
            response = builder.get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new ServiceException(response.readEntity(String.class));
            }

            if (response.getHeaderString(HttpHeaders.CACHE_CONTROL) != null) {
                // Update cache expiry
                CacheControl cacheControl = CacheControl.valueOf(response.getHeaderString(HttpHeaders.CACHE_CONTROL));
                Date dateRecieved = response.getDate();
                _concertCacheExpiry = new Date();
                _concertCacheExpiry.setTime(dateRecieved.getTime() + cacheControl.getMaxAge() * 1000);
            }

            // Update Cache
            _concertCache = response.readEntity(new GenericType<Set<ConcertDTO>>() {});
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        try {
            // Check cache
            if (_performerCacheExpiry == null || _performerCache == null) {
                _logger.debug("First time request, getting performers");
                // make request and set expiry
                this.revalidatePerformerCache();
            } else if (new Date().after(_performerCacheExpiry)) {
                _logger.debug("Cache out of date revalidating");
                // cache is invalidated revalidate
                this.revalidatePerformerCache();
            }
            return _performerCache;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    private void revalidatePerformerCache() throws ServiceException {
        _logger.debug("Update performers cache");
        Response response;
        Client client = ClientBuilder.newClient();

        try {
            Builder builder = client.target(PERFORMER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);
            response = builder.get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new ServiceException(response.readEntity(String.class));
            }

            if (response.getHeaderString(HttpHeaders.CACHE_CONTROL) != null) {
                // Update cache expiry
                CacheControl cacheControl = CacheControl.valueOf(response.getHeaderString(HttpHeaders.CACHE_CONTROL));
                Date dateRecieved = response.getDate();
                _performerCacheExpiry = new Date();
                _performerCacheExpiry.setTime(dateRecieved.getTime() + cacheControl.getMaxAge() * 1000);
            }

            // Update Cache
            _performerCache = response.readEntity(new GenericType<Set<PerformerDTO>>() {});
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

            return response.readEntity(ReservationDTO.class);
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
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            if (_authToken == null) {
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            }

            Builder builder = client.target(BOOKING_CONFIRMATION_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making reservation confirmation request to " + BOOKING_CONFIRMATION_SERVICE);

            response = builder
                    .cookie("AuthToken", _authToken.getValue())
                    .post(Entity.entity(reservation, MediaType.APPLICATION_XML));

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _logger.debug("Successfully booked tickets!");
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
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            if (_authToken == null) {
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            }

            Builder builder = client.target(CREDIT_CARD_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            response = builder
                    .cookie("AuthToken", _authToken.getValue())
                    .post(Entity.entity(creditCard, MediaType.APPLICATION_XML));

            // Throw appropriate exception baseed on response
            if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

        } catch (Exception e) {
            if (e instanceof  ServiceException) {
                throw e;
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        if (_authToken == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        try {
            // Check cache
            if (_bookingCacheExpiry == null || _bookingCache == null) {
                _logger.debug("First time request, getting bookings");
                // make request and set expiry
                this.revalidateBookingCache();
            } else if (new Date().after(_bookingCacheExpiry)) {
                _logger.debug("Cache out of date revalidating");
                // cache is invalidated revalidate
                this.revalidateBookingCache();
            }
            _logger.debug("returning bookings");
            return _bookingCache;
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        }
    }

    private void revalidateBookingCache() throws ServiceException {
        _logger.debug("Update booking cache");
        Response response;
        Client client = ClientBuilder.newClient();

        try {
            Builder builder = client.target(BOOKINGS_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            response = builder
                    .cookie("AuthToken", _authToken.getValue())
                    .get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            if (response.getHeaderString(HttpHeaders.CACHE_CONTROL) != null) {
                // Update cache expiry
                CacheControl cacheControl = CacheControl.valueOf(response.getHeaderString(HttpHeaders.CACHE_CONTROL));
                Date dateRecieved = response.getDate();
                _bookingCacheExpiry = new Date();
                _bookingCacheExpiry.setTime(dateRecieved.getTime() + cacheControl.getMaxAge() * 1000);
            }

            // Update Cache
            _bookingCache = response.readEntity(new GenericType<Set<BookingDTO>>() {});
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
    public void createNewsItem(NewsItemDTO newsItemDTO) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(NEWS_ITEM_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Create news item request ");

            response = builder.post(Entity.entity(newsItemDTO, MediaType.APPLICATION_XML));

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _logger.debug("Created news Item " + newsItemDTO.getTitle() + " At " + response.getLocation());
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
    public void cancelNewsItemSub() throws ServiceException{
        Client client = ClientBuilder.newClient();
        Response response;
        try {
            if (_authToken == null) {
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            }

            Builder builder = client.target(NEWS_ITEM_SUB_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making create delete sub request");

            response = builder
                    .cookie("AuthToken" , _authToken.getValue())
                    .delete();
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _logger.debug("Successfully removed subscription");
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
    public void newsItemSub() throws ServiceException{
        Client client = ClientBuilder.newClient();

        try {
            if (_authToken == null) {
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            }
            _logger.debug("Creating new sub");
            WebTarget target = client.target(NEWS_ITEM_SUB_SERVICE);

            client.target(NEWS_ITEM_SUB_SERVICE)
                    .request()
                    .accept(MediaType.APPLICATION_XML)
                    .cookie("AuthToken", _authToken.getValue())
                    .async()
                    .get(new NewsItemCallback(target, _subscription, this._authToken));
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        }
    }

    public NewsItemDTO getCurrentNewsItem() {
        return _subscription.getNewsItemDTO();
    }
}
