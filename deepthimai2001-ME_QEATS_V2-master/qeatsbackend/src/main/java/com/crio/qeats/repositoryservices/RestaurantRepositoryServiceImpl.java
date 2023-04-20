package com.crio.qeats.repositoryservices;



// import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoUtils;



import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;



import java.util.List;



import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;



import org.springframework.stereotype.Service;
// import redis.clients.jedis.Jedis;



@Primary
@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



 // @Autowired
 // private RedisConfiguration redisConfiguration;



 @Autowired
 private Provider<ModelMapper> modelMapperProvider;



 @Autowired
 private RestaurantRepository restaurantRepository;



 private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
 LocalTime openingTime = LocalTime.parse(res.getOpensAt());
 LocalTime closingTime = LocalTime.parse(res.getClosesAt());



 return time.isAfter(openingTime) && time.isBefore(closingTime);
 }



 @Override
 public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
 LocalTime currentTime, Double servingRadiusInKms) {
 return findAllRestaurantsMongo(latitude, longitude, currentTime, servingRadiusInKms);



 }



 public List<Restaurant> findAllRestaurantsMongo(Double latitude, Double longitude,
 LocalTime currentTime, Double servingRadiusInKms) {
 System.out.println("------in MONGO function -------");
 List<Restaurant> restaurants = new ArrayList<Restaurant>();
 ObjectMapper objectMapper = new ObjectMapper();



 List<RestaurantEntity> allRestaurants = restaurantRepository.findAll();



 for (RestaurantEntity restaurantEntity : allRestaurants) {
 if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
 servingRadiusInKms)) {
 Restaurant restaurant = modelMapperProvider.get().map(restaurantEntity, Restaurant.class);
 restaurants.add(restaurant);
 }
 }



 String restaurantDbString = "";
 try {
 restaurantDbString = objectMapper.writeValueAsString(restaurants);
 } catch (IOException e) {
 e.printStackTrace();
 }
 return restaurants;
 }



 /**
 * Utility method to check if a restaurant is within the serving radius at a given time.
 *
 * @return boolean True if restaurant falls within serving radius and is open, false otherwise
 */
 private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
 LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
 if (isOpenNow(currentTime, restaurantEntity)) {
 return GeoUtils.findDistanceInKm(latitude, longitude, restaurantEntity.getLatitude(),
 restaurantEntity.getLongitude()) < servingRadiusInKms;
 }



 return false;
 }



}