package com.example.Auc.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;
    private Double bidAmount;
    private String bidder;

    private LocalDateTime bidTime = LocalDateTime.now();
}