package com.example.api.albums.io.controllers;

import com.example.api.albums.data.AlbumEntity;
import com.example.api.albums.service.AlbumsService;
import com.example.api.albums.ui.model.AlbumResponseModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users/{id}/albums")
public class AlbumsController {

    private static final Logger LOG = LoggerFactory.getLogger(AlbumsController.class);

    private final AlbumsService albumsService;


    public AlbumsController(AlbumsService albumsService) {
        this.albumsService = albumsService;
    }
    
    @PreAuthorize("principal == #id or hasAuthority('PROFILE.READ') or hasRole('ADMIN')")
    @GetMapping(
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE,
            })
    public List<AlbumResponseModel> userAlbums(@PathVariable("id") String id) {

        List<AlbumResponseModel> returnValue = new ArrayList<>();

        List<AlbumEntity> albumsEntities = albumsService.getAlbums(id);

        if (albumsEntities == null || albumsEntities.isEmpty()) {
            return returnValue;
        }

        Type listType = new TypeToken<List<AlbumResponseModel>>() {
        }.getType();

        returnValue = new ModelMapper().map(albumsEntities, listType);
        LOG.info("Returning " + returnValue.size() + " albums");
        return returnValue;
    }
}
