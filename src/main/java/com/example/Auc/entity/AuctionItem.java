package com.example.Auc.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class AuctionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemTitle;

    @Column(name = "item_condition")
    private String itemCondition;

    @Lob
    @Column(length = 2048)
    private String description;

    private double startingPrice;
    private int auctionDuration;
    private String phone;
    private String preferredMeetingLocation;

    @ElementCollection
    private List<String> imageUrls;

    private Double currentBid;

    private LocalDateTime endTime;

    @ManyToOne            //   ???
    @JoinColumn(name = "seller_id")
    private User seller;
}