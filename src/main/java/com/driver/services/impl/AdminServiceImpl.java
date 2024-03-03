package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setServiceProviders(new ArrayList<>());
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Admin admin = adminRepository1.findById(adminId).get();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setAdmin(admin);
        serviceProvider.setName(providerName);
        serviceProvider.setUsers(new ArrayList<>());
        serviceProvider.setConnectionList(new ArrayList<>());
        serviceProvider.setCountryList(new ArrayList<>());
        admin.getServiceProviders().add(serviceProvider);
        serviceProviderRepository1.save(serviceProvider);
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{
        ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();
        CountryName Name = null;
        try {
            Name = CountryName.valueOf(countryName);
        }catch (Exception e){
            throw new Exception("Country not found");
        }
        for(CountryName name :CountryName.values()){
            if(Name.equals(name)){
                Country country = new Country();
                country.setCountryName(Name);
                country.setServiceProvider(serviceProvider);
                country.setCode(name.toCode());
                serviceProvider.getCountryList().add(country);
                countryRepository1.save(country);
                serviceProviderRepository1.save(serviceProvider);
                return serviceProvider;
            }
        }
        throw new Exception("Country not found");
    }
}
