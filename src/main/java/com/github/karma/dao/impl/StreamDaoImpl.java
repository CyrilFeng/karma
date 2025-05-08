package com.github.karma.dao.impl;

import cn.hutool.json.JSONUtil;
import com.github.karma.dao.StreamDao;
import com.github.karma.dto.Stream;
import com.github.karma.dto.content.Content;
import com.github.karma.mapper.StreamMapper;
import com.github.karma.mapper.entity.StreamDO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StreamDaoImpl implements StreamDao {

    @Autowired
    private StreamMapper streamMapper;

    @Override
    public void insertSteam(Stream analyseSteam) {
        StreamDO streamDO = convert(analyseSteam);
        streamMapper.insert(streamDO);
    }

    @Override
    public void updateSteam(Stream analyseSteam) {
        StreamDO streamDO = convert(analyseSteam);
        streamMapper.updateById(streamDO);
    }

    @Override
    public void deleteSteam(Long analyseStreamId) {
        streamMapper.deleteById(analyseStreamId);
    }

    @Override
    public Stream getSteam(Long analyseStreamId) {
        StreamDO streamDO = streamMapper.selectById(analyseStreamId);
        if (streamDO == null) {
            return null;
        }
        return convert(streamDO);
    }

    // DTO -> DO
    private StreamDO convert(Stream stream) {
        StreamDO streamDO = new StreamDO();
        BeanUtils.copyProperties(stream, streamDO);
        streamDO.setContent(JSONUtil.toJsonStr(stream.getContent()));
        return streamDO;
    }

    // DO -> DTO
    private Stream convert(StreamDO streamDO) {
        Stream stream = new Stream();
        BeanUtils.copyProperties(streamDO, stream);
        stream.setContent(JSONUtil.toBean(streamDO.getContent(), Content.class));
        return stream;
    }

}
