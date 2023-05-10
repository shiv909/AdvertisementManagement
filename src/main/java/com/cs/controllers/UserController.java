package com.cs.controllers;

import com.cs.entities.Advertisement;
import com.cs.entities.User;
import com.cs.helper.Message;
import com.cs.repositories.AdvertisementRepository;
import com.cs.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @GetMapping("/dashboard")
    public ModelAndView userDashboard(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("normal/userDashboard");
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        modelAndView.addObject("user", user);
        modelAndView.addObject("title", "Dashboard");

        Iterable<User> users = userRepository.findAll();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }

        return modelAndView;
    }

    @GetMapping("/add-advertisement")
    public ModelAndView openAddAdvertisementForm(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("normal/addAdvertisement");
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        modelAndView.addObject("user", user);
        modelAndView.addObject("title", "Add Advertisement");
        modelAndView.addObject("advertisement", new Advertisement());
        return modelAndView;

    }

    @PostMapping("/process-advertisement")
    public ModelAndView processAdvertisement(@ModelAttribute Advertisement advertisement, @RequestPart("AdvertisementImage") MultipartFile file, Principal principal, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("normal/addAdvertisement");
        try {
            advertisement.setLocalDateTime(LocalDateTime.now());
            String userName = principal.getName();
            User user = userRepository.getUserByUserName(userName);
            if (!file.isEmpty()) {
                advertisement.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/image").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            advertisement.setUser(user);
            user.getAdvertisements().add(advertisement);
            userRepository.save(user);
            modelAndView.addObject("user", user);
            modelAndView.addObject("title", "Add Advertisement");
            modelAndView.addObject("advertisement", new Advertisement());
            session.setAttribute("message", new Message("Advertisement is added !! Add new one", "success"));
            return modelAndView;
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("something went wrong!", "danger"));
        }
        return modelAndView;
    }

    //To show all advertisements
    @GetMapping("/showAdvertisements/{page}")
    public ModelAndView showAdvertisement(@PathVariable("page") Integer page, Principal principal) {
        ModelAndView modelAndView = new ModelAndView("normal/showAdvertisements");
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        Pageable pageable = PageRequest.of(page, 5);
        Page<Advertisement> advertisementList = advertisementRepository.findAdvertisementByUser(user.getUserId(), pageable);
        modelAndView.addObject("advertisementList", advertisementList);
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPages", advertisementList.getTotalPages());
        modelAndView.addObject("user", user);
        modelAndView.addObject("title", "show Advertisements");
        modelAndView.addObject("advertisement", new Advertisement());
        return modelAndView;
    }

    //To delete the advertisement
    @GetMapping("/delete/{advertisementId}")
    public RedirectView deleteAdvertisement(@PathVariable("advertisementId") Integer advertisementId, RedirectView redirectView) {
        Optional<Advertisement> byId = advertisementRepository.findById(advertisementId);
        Advertisement advertisement = byId.get();
        advertisementRepository.delete(advertisement);
        redirectView.setUrl("/user/showAdvertisements/0");
        return redirectView;
    }

    @PostMapping("/update/{advertisementId}")
    public ModelAndView updateAdvertisement(@PathVariable("advertisementId") Integer advertisementId, Principal principal) {
        ModelAndView modelAndView = new ModelAndView("normal/UpdateAdvertisement");
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        Optional<Advertisement> byId = advertisementRepository.findById(advertisementId);
        Advertisement advertisement = byId.get();
        modelAndView.addObject("advertisement", advertisement);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    //To update the advertisement
    @PostMapping("/process-update")
    public RedirectView updateAdvertisementHandler(@ModelAttribute Advertisement advertisement, @RequestPart("AdvertisementImage") MultipartFile file, RedirectView redirectView, Principal principal) throws IOException {
        if (!file.isEmpty()) {
            File saveFile = new ClassPathResource("/static/image").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            advertisement.setImage(file.getOriginalFilename());
        }
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        advertisement.setLocalDateTime(LocalDateTime.now());
        advertisement.setUser(user);
        advertisementRepository.save(advertisement);
        redirectView.setUrl("/user/showAdvertisements/0");
        return redirectView;
    }

    //To show the user profile
    @GetMapping("/show-user-profile")
    public ModelAndView showUserProfile(Principal principal){
        ModelAndView modelAndView = new ModelAndView("normal/showUserProfile");
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        modelAndView.addObject("user",user);
        return modelAndView;
    }

    @PostMapping("/process-user-profile")
    public ModelAndView processUserProfile(@RequestPart("profilePicture") MultipartFile file, Principal principal) throws IOException {
        ModelAndView modelAndView = new ModelAndView("normal/showUserProfile");
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        if (!file.isEmpty()) {
            user.setProfile(file.getOriginalFilename());
            File saveFile = new ClassPathResource("static/image").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }
        userRepository.save(user);
        modelAndView.addObject("user",user);
        return modelAndView;
    }
}