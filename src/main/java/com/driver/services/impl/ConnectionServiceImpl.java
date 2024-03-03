package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();
        if(user.getConnected()) throw new Exception("Already connected");
        else if(user.getOriginalCountry().getCountryName().equals(CountryName.valueOf(countryName))) return user;
        else if(user.getServiceProviderList()==null ) throw new Exception("Unable to connect");
        List<ServiceProvider> serviceProviders = user.getServiceProviderList();
        int serviceProviderId= Integer.MAX_VALUE;
        ServiceProvider serviceProviderans =null;
        String countrycode = "" ;
        boolean count = false;
        for (ServiceProvider serviceProvider:serviceProviders){
            List<Country> countries = serviceProvider.getCountryList();
            for(Country country : countries){
                if(country.getCountryName().equals(CountryName.valueOf(countryName))){
                    count=true;
                    if(serviceProvider.getId()<serviceProviderId){
                        serviceProviderId = serviceProvider.getId();
                        countrycode = country.getCode();
                        serviceProviderans = serviceProvider;
                        break;
                    }
                }
            }
        }
        if(!count) throw new Exception("Unable to connect");
        user.setConnected(true);
        user.setMaskedIp(countrycode+"."+serviceProviderId+"."+user.getId());
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(serviceProviderans);
        connectionRepository2.save(connection);
        user.getConnectionList().add(connection);
        userRepository2.save(user);
        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if(!user.getConnected()) throw new Exception("Already disconnected");
        user.setMaskedIp(null);
        user.setConnected(false);
        List<Connection> connections = user.getConnectionList();
        for(Connection connection : connections){
            connectionRepository2.deleteById(connection.getId());
        }
        userRepository2.save(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();
        Country currCountry = null;
        if(!receiver.getConnected()) currCountry = receiver.getOriginalCountry();
        else {
            String arr[] = receiver.getMaskedIp().split(".");
            String code = arr[0];
            ServiceProvider serviceProvider = serviceProviderRepository2.findById(Integer.parseInt(arr[1])).get();
            List<Country> countries = serviceProvider.getCountryList();
            for(Country country : countries){
                if(country.getCode().equals(code)){
                    currCountry = country;
                }
            }
        }
        if(currCountry==null) throw new Exception("Cannot establish communication");
        else if(sender.getOriginalCountry().equals(currCountry)) return sender;
        else{
            return connect(senderId, currCountry.getCountryName().toString());
        }
    }
}
