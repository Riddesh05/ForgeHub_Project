package com.example.ForgeHubs.config;

import com.example.ForgeHubs.DTO.RFQItemResponseDto;
import com.example.ForgeHubs.DTO.RFQResponseDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQItem;
import com.example.ForgeHubs.Entity.User;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        TypeMap<User, UserResponseDto> userMap =
                modelMapper.createTypeMap(User.class, UserResponseDto.class);
        userMap.addMapping(User::getUserId, UserResponseDto::setUserId);
        userMap.addMapping(User::getName, UserResponseDto::setName);
        userMap.addMapping(User::getEmail, UserResponseDto::setEmail);
        userMap.addMapping(User::getRole, UserResponseDto::setRole);
        userMap.setPostConverter(context -> {
            User source = context.getSource();
            UserResponseDto destination = context.getDestination();
            destination.setFirstTimeLogin(Boolean.TRUE.equals(source.isFirstTimeLogin()));
            destination.setTotpConfigured(
                    source.getSecretKey() != null && !source.getSecretKey().isBlank()
            );
            return destination;
        });

        // RFQItem has matching property names, so ModelMapper handles the complete DTO mapping.
        modelMapper.createTypeMap(RFQItem.class, RFQItemResponseDto.class);

        Converter<List<RFQItem>, List<RFQItemResponseDto>> rfqItemsConverter = context ->
                context.getSource() == null
                        ? Collections.emptyList()
                        : context.getSource().stream()
                                .map(item -> modelMapper.map(item, RFQItemResponseDto.class))
                                .toList();

        TypeMap<RFQ, RFQResponseDto> rfqMap =
                modelMapper.createTypeMap(RFQ.class, RFQResponseDto.class);
        rfqMap.addMappings(mapper -> mapper
                .using(rfqItemsConverter)
                .map(RFQ::getItems, RFQResponseDto::setItems));
        rfqMap.setPostConverter(context -> {
            RFQ source = context.getSource();
            RFQResponseDto destination = context.getDestination();
            destination.setDeleted(Boolean.TRUE.equals(source.getIsDeleted()));
            return destination;
        });

        return modelMapper;
    }
}
