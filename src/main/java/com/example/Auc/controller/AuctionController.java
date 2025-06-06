package com.example.Auc.controller;

import com.example.Auc.entity.AuctionItem;
import com.example.Auc.entity.Bid;
import com.example.Auc.entity.User;
import com.example.Auc.repository.AuctionItemRepository;
import com.example.Auc.repository.BidRepository;
import com.example.Auc.repository.UserRepository;
//import com.example.Auc.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AuctionController {

    @Autowired
    private AuctionItemRepository auctionItemRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private S3Service s3Service;

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName());
    }

    @GetMapping("/auctions")
    public String showAuctions(Model model, Principal principal) {
        LocalDateTime now = LocalDateTime.now();
        List<AuctionItem> activeAuctions = auctionItemRepository.findAll().stream()
                .filter(item -> item.getEndTime() != null && item.getEndTime().isAfter(now))
                .collect(Collectors.toList());
        model.addAttribute("auctionItems", activeAuctions);
        model.addAttribute("user", getCurrentUser(principal));
        return "auctions";
    }

    @GetMapping("/auction/{id}")
    public String auctionDetails(@PathVariable Long id, Model model, Principal principal) {
        AuctionItem item = auctionItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Auction item not found"));
        User currentUser = getCurrentUser(principal);

        List<Bid> bids = bidRepository.findAll().stream()
                .filter(b -> b.getItemId().equals(id))
                .collect(Collectors.toList());
        Bid highestBid = bids.stream()
                .max(Comparator.comparingDouble(Bid::getBidAmount))
                .orElse(null);

        User winner = null;
        boolean isWinner = false;
        boolean auctionEnded = item.getEndTime() != null && item.getEndTime().isBefore(LocalDateTime.now());
        if (highestBid != null) {
            winner = userRepository.findByEmail(highestBid.getBidder());
            isWinner = auctionEnded && currentUser != null && winner != null
                    && currentUser.getEmail().equals(winner.getEmail());
        }

        Double minBid = item.getCurrentBid() != null ? item.getCurrentBid() : item.getStartingPrice();
        Double maxAllowedBid = Math.min(minBid + 5000, 999999.0);

        model.addAttribute("auctionItem", item);
        model.addAttribute("user", currentUser);
        model.addAttribute("winner", winner);
        model.addAttribute("isWinner", isWinner);
        model.addAttribute("auctionEnded", auctionEnded);
        model.addAttribute("minBid", minBid);
        model.addAttribute("maxAllowedBid", maxAllowedBid);
        return "auctiondetails";
    }

    @GetMapping("/sell")
    public String showSellPage(Model model, Principal principal) {
        model.addAttribute("user", getCurrentUser(principal));
        return "sell";
    }

    @PostMapping("/sell")
    public String createAuction(
            @RequestParam String itemTitle,
            @RequestParam("itemCondition") String itemCondition,
            @RequestParam String description,
            @RequestParam double startingPrice,
            @RequestParam int auctionDuration,
            @RequestParam String phone,
            @RequestParam String preferredMeetingLocation,
            @RequestParam("images") MultipartFile[] images,
            Principal principal
    ) {
        if (images.length < 2 || images.length > 8) {
            return "redirect:/sell?error=ImageCount";
        }
        if (startingPrice > 999999) {
            return "redirect:/sell?error=PriceLimit";
        }
// from here it starts
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String url = uploadToImgBB(image);
            if (url != null) imageUrls.add(url);
        }

//uAWS S3 service
//        List<String> imageUrls = new ArrayList<>();
//        try {
//            for (MultipartFile image : images) {
//                String url = s3Service.uploadFile(image);
//                if (url != null) imageUrls.add(url);
//            }
//        } catch (Exception e) {
//            return "redirect:/sell?error=ImageUpload";
//        }


        AuctionItem item = new AuctionItem();
        item.setItemTitle(itemTitle);
        item.setItemCondition(itemCondition);
        item.setDescription(description);
        item.setStartingPrice(startingPrice);
        item.setAuctionDuration(auctionDuration);
        item.setPhone(phone);
        item.setPreferredMeetingLocation(preferredMeetingLocation);
        item.setImageUrls(imageUrls);
        item.setSeller(getCurrentUser(principal));
        if (auctionDuration == 0) {
            item.setEndTime(LocalDateTime.now().plusMinutes(5));
        } else {
            item.setEndTime(LocalDateTime.now().plusDays(auctionDuration));
        }
        auctionItemRepository.save(item);
        return "redirect:/auctions";
    }


    //check this once, using image BB dont know if it'll work as intended tho,
// youtube videos say it works for small projects, cloud is too much of a hassle to omplement

    private String uploadToImgBB(MultipartFile image) {
        try {
            String apiKey = "db6517868d1a2af5b75ec679814ba59b";
            String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", Base64.getEncoder().encodeToString(image.getBytes()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            Map data = (Map) response.getBody().get("data");

            return (String) data.get("url");
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/bid")
    public String placeBid(@RequestParam Long itemId,
                           @RequestParam Double bidAmount,
                           Principal principal,
                           Model model) {
        AuctionItem item = auctionItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Auction item not found"));
        User currentUser = getCurrentUser(principal);

        if (item.getSeller() != null && currentUser != null && item.getSeller().getId().equals(currentUser.getId())) {
            return "redirect:/auction/" + itemId + "?bidError=You cannot bid on your own auction.";
        }
        Double minBid = item.getCurrentBid() != null ? item.getCurrentBid() : item.getStartingPrice();
        Double maxAllowedBid = Math.min(minBid + 5000, 999999.0);

        if (bidAmount > minBid && bidAmount <= maxAllowedBid) {
            Bid bid = new Bid();
            bid.setItemId(itemId);
            bid.setBidAmount(bidAmount);
            bid.setBidder(principal != null ? principal.getName() : "anonymous");
            bid.setBidTime(LocalDateTime.now());
            bidRepository.save(bid);

            item.setCurrentBid(bidAmount);
            auctionItemRepository.save(item);
        } else {
            return "redirect:/auction/" + itemId + "?bidError=Bid must be higher than current bid and not exceed ₹5000 increment or ₹999999 total.";
        }

        return "redirect:/auction/" + itemId;
    }

    @GetMapping("/mybids")
    public String showMyBids(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        String userEmail = user.getEmail();
        List<Bid> allBids = bidRepository.findAll();
        Map<Long, List<Bid>> bidsGroupedByItem = allBids.stream()
                .collect(Collectors.groupingBy(Bid::getItemId));

        List<AuctionItem> allItems = auctionItemRepository.findAll();
        int activeBids = 0, outbid = 0, wonAuctions = 0;
        List<AuctionItem> myActiveBids = new ArrayList<>();
        List<AuctionItem> myOutbids = new ArrayList<>();
        List<AuctionItem> myWonAuctions = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (AuctionItem item : allItems) {
            List<Bid> bidsForItem = bidsGroupedByItem.getOrDefault(item.getId(), Collections.emptyList());

            Bid highestBid = bidsForItem.stream()
                    .max(Comparator.comparingDouble(Bid::getBidAmount))
                    .orElse(null);

            LocalDateTime auctionStart = bidsForItem.stream()
                    .map(Bid::getBidTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            boolean isActive = item.getEndTime() != null && item.getEndTime().isAfter(now);

            boolean userHasBid = bidsForItem.stream().anyMatch(b -> b.getBidder().equals(userEmail));

            if (userHasBid) {
                if (isActive) {
                    if (highestBid != null && highestBid.getBidder().equals(userEmail)) {
                        activeBids++;
                        myActiveBids.add(item);
                    } else {
                        outbid++;
                        myOutbids.add(item);
                    }
                } else {
                    if (highestBid != null && highestBid.getBidder().equals(userEmail)) {
                        wonAuctions++;
                        myWonAuctions.add(item);
                    }
                }
            }
        }

        List<AuctionItem> myActiveAuctions = auctionItemRepository.findAll().stream()
                .filter(item -> item.getSeller() != null
                        && item.getSeller().getId().equals(user.getId()))
                .collect(Collectors.toList());
        model.addAttribute("activeBids", activeBids);
        model.addAttribute("outbid", outbid);
        model.addAttribute("wonAuctions", wonAuctions);
        model.addAttribute("myActiveBids", myActiveBids);
        model.addAttribute("myOutbids", myOutbids);
        model.addAttribute("myWonAuctions", myWonAuctions);
        model.addAttribute("myActiveAuctions", myActiveAuctions);
        model.addAttribute("user", user);

        return "mybids";
    }
}