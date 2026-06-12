package com.civicconnect.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "departments")
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "slug")
    private String slug;
    @Column(name = "icon")
    private String icon;
    @Column(name = "color")
    private String color;
    @Column(name = "description")
    private String description;
    @Column(name = "hod_name")
    private String hodName;
    @Column(name = "hod_email")
    private String hodEmail;
    @Column(name = "hod_phone")
    private String hodPhone;
    @Column(name = "hod_title")
    private String hodTitle;

}
