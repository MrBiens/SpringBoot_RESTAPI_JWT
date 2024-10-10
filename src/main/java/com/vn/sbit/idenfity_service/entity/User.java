package com.vn.sbit.idenfity_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor //constructor null
@AllArgsConstructor // constuctor đầy đủ thuộc tính
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
     String id;

     String userName;

     String passWord;

     String firstName;

     String lastName;

     LocalDate dob;





}
