package com.kaleidoscope.tripserver.pojos;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Trip implements TripItem{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
}
