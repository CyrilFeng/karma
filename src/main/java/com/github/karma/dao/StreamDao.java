package com.github.karma.dao;

import com.github.karma.dto.Stream;


public interface StreamDao {

    void insertSteam(Stream analyseSteam);

    void updateSteam(Stream analyseSteam);

    void deleteSteam(Long analyseStreamId);

    Stream getSteam(Long analyseStreamId);

}
