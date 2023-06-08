package com.tafakkoor.e_learn.services;

import com.google.gson.Gson;
import com.tafakkoor.e_learn.domain.*;
import com.tafakkoor.e_learn.dto.UpdateUserDTO;
import com.tafakkoor.e_learn.dto.UserRegisterDTO;
import com.tafakkoor.e_learn.enums.ContentType;
import com.tafakkoor.e_learn.enums.Levels;
import com.tafakkoor.e_learn.enums.Progress;
import com.tafakkoor.e_learn.enums.Status;
import com.tafakkoor.e_learn.repository.*;
import com.tafakkoor.e_learn.utils.Util;
import com.tafakkoor.e_learn.utils.mail.EmailService;
import com.tafakkoor.e_learn.vos.FacebookVO;
import com.tafakkoor.e_learn.vos.GoogleVO;
import com.tafakkoor.e_learn.vos.LinkedInVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
public class UserService {
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ImageService imageService;
    private final UserContentRepository userContentRepository;
    private final ContentRepository contentRepository;
    private final CommentRepository commentRepository;
    private final VocabularyRepository vocabularyRepository;
    private final QuestionsRepository questionRepository;
    private final OptionRepository optionRepository;
    private final AuthRoleRepository authRoleRepository;

    private final Gson gson = Util.getInstance().getGson();

    public UserService(AuthUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenRepository tokenRepository,
                       TokenService tokenService,
                       UserContentRepository userContentRepository,
                       ContentRepository contentRepository,
                       CommentRepository commentRepository,
                       VocabularyRepository vocabularyRepository, ImageService imageService, QuestionsRepository questionRepository, OptionRepository optionRepository, AuthRoleRepository authRoleRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.imageService = imageService;
        this.userContentRepository = userContentRepository;
        this.contentRepository = contentRepository;
        this.commentRepository = commentRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.optionRepository = optionRepository;
        this.authRoleRepository = authRoleRepository;
    }

    public List<Levels> getLevels(@NonNull Levels level) {
        List<Levels> levelsList = new ArrayList<>();
        if (level.equals(Levels.DEFAULT)) {
            return levelsList;
        }
        Levels[] levels = Levels.values();
        for (int i = 0; i < levels.length; i++) {
            if (!levels[i].equals(level)) {
                levelsList.add(levels[i]);
            } else {
                levelsList.add(levels[i]);
                break;
            }
        }
        return levelsList;
    }

    public AuthUser getUser(Long id) {
        return userRepository.findById(id);
    }

    public void saveUserAndSendEmail(UserRegisterDTO dto) {
        AuthUser user = AuthUser.builder()
                .username(dto.getUsername().toLowerCase())
                .firstName(dto.getFirstname())
                .lastName(dto.getLastname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail().toLowerCase())
                .score(0)
                .build();
        userRepository.save(user);
        sendActivationEmail(user);
    }

    public void sendActivationEmail(AuthUser authUser) {
        Util util = Util.getInstance();
        String token = tokenService.generateToken();  // TODO: 3/12/23 encrypt token
        String email = authUser.getEmail();
        String body = util.generateBody(authUser.getFirstName().concat(" ").concat(authUser.getLastName()), token);
        tokenService.save(util.buildToken(token, authUser));
        CompletableFuture.runAsync(() -> EmailService.getInstance().sendEmail(email, body, "Activation Email"));
    }


    public List<Content> getContentsStories(Levels level, Long id) throws RuntimeException{
        UserContent userContent = checkUserStatus(id);
        if (userContent != null) {
            Content content = userContent.getContent();
            throw new RuntimeException("You have a content in progress named \"%s\". Please complete the content first. id=%d".formatted(content.getTitle(), content.getId()));
        }
        return contentRepository.findByLevelAndContentTypeAndDeleted(level, ContentType.STORY, false);
    }

    public ModelAndView getInProgressPage(ModelAndView modelAndView, Exception e) {
        String eMessage = e.getMessage();
        Long id = Long.parseLong(eMessage.substring(eMessage.indexOf("id") + 3));
        modelAndView.addObject("inProgress", eMessage.substring(0, eMessage.indexOf("id")));
        modelAndView.addObject("content", getContent(id).get());
        modelAndView.setViewName("user/inProgress");
        return modelAndView;
    }

    public UserContent checkUserStatus(Long id) {
        return userContentRepository.findByUserIdAndProgressOrProgress(id, Progress.IN_PROGRESS, Progress.TAKE_TEST);
    }

    public List<Content> getContentsGrammar(Levels level, Long id) {
        UserContent userContent = checkUserStatus(id);
        if (userContent != null) {
            Content content = userContent.getContent();
            throw new RuntimeException("You have a content in progress named \"%s\". Please complete the content first. id=%d".formatted(content.getTitle(), content.getId()));
        }
        return contentRepository.findByLevelAndContentTypeAndDeleted(level, ContentType.GRAMMAR, false);
    }

    public Optional<Content> getContent(Long id) {
        return contentRepository.findById(id);
    }

    public Optional<Content> getContent(String storyId, Long userId) {
        Optional<Content> content = contentRepository.findById(Long.valueOf(storyId));
        return Optional.empty();
    }



    public List<AuthUser> getAllUsers() {
        return userRepository.findByDeleted(false);
    }

    public void updateStatus(Long id) {
        AuthUser byId = userRepository.findById(id);
        boolean blocked = byId.getStatus().equals(Status.BLOCKED);
        if (blocked) {
            byId.setStatus(Status.ACTIVE);
        } else {
            byId.setStatus(Status.BLOCKED);
        }
        userRepository.save(byId);
    }


    public Content getStoryById(Long id) {
        return contentRepository.findByIdAndContentType(id, ContentType.STORY);
    }

    public List<Comment> getComments(Long id) {
        return Objects.requireNonNullElse(commentRepository.findAllByContentIdAndDeleted(id, false), new ArrayList<>());
    }

    public void addComment(Comment comment) {
        commentRepository.saveComment(comment.getComment(), String.valueOf(comment.getCommentType()), comment.getUserId().getId(), comment.getContentId(), comment.getParentId());
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public void deleteCommentById(Long id) {
        commentRepository.setAsDelete(id);
    }

    public void updateComment(Comment comment1) {
        commentRepository.updateComment(comment1.getComment(), comment1.getId());
    }
    // Abdullo's code below that


    public boolean userExist(String username) {
        AuthUser user = userRepository.findByUsername(username);
        return user != null;
    }

    public void saveGoogleUser(String userInfo) {
        Image image = null;
        GoogleVO googleUser = gson.fromJson(userInfo, GoogleVO.class);
        String email = googleUser.getEmail();
        String username = googleUser.getSub();
        if (userExist(username)) {
            changeLastLogin(username);
            return;
        }

        if (googleUser.getPicture() != null)
            image = imageService.buildAndSaveImage(googleUser.getPicture(), "google" + googleUser.getSub());

        String password = "the.Strongest.Password@Ever9";
        AuthUser user = AuthUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .firstName(googleUser.getGiven_name())
                .lastName(googleUser.getFamily_name())
                .image(image)
                .status(Status.ACTIVE)
                .isOAuthUser(true)
                .lastLogin(LocalDateTime.now(ZoneId.of("Asia/Tashkent")))
                .build();
        userRepository.save(user);
    }

    public void saveFacebookUser(String userInfo) {
        Image image = null;
        FacebookVO facebookUser = gson.fromJson(userInfo, FacebookVO.class);
        String email = facebookUser.getEmail();
        String username = facebookUser.getId();
        if (userExist(username)) {
            changeLastLogin(username);
            return;
        }

        if (facebookUser.getPicture_large() != null)
            image = imageService.buildAndSaveImage(facebookUser.getPicture_large().getData().getUrl(), "facebook" + facebookUser.getId());

        String password = "the.Strongest.Password@Ever9";
        AuthUser user = AuthUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .firstName(facebookUser.getFirst_name())
                .lastName(facebookUser.getLast_name())
                .image(image)
                .status(Status.ACTIVE)
                .lastLogin(LocalDateTime.now(ZoneId.of("Asia/Tashkent")))
                .isOAuthUser(true)
                .build();
        userRepository.save(user);
    }

    public String saveLinkedinUser(String userInfo) {
        Image image = null;
        LinkedInVO linkedInUser = gson.fromJson(userInfo, LinkedInVO.class);
        String email = linkedInUser.getEmail();
        String username = linkedInUser.getSub();
        if (userExist(username)) {
            changeLastLogin(username);
            return username;
        }

        if (linkedInUser.getPicture() != null)
            image = imageService.buildAndSaveImage(linkedInUser.getPicture(), "linkedIn" + username);
        String password = "the.Strongest.Password@Ever9";
        AuthUser user = AuthUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .firstName(linkedInUser.getGiven_name())
                .lastName(linkedInUser.getFamily_name())
                .image(image)
                .status(Status.ACTIVE)
                .lastLogin(LocalDateTime.now(ZoneId.of("Asia/Tashkent")))
                .isOAuthUser(true)
                .build();
        userRepository.save(user);
        return username;
    }

    public void changeLastLogin(String username) {
        AuthUser user = userRepository.findByUsername(username);
        user.setLastLogin(LocalDateTime.now(ZoneId.of("Asia/Tashkent")));
        userRepository.save(user);
    }

    public AuthUser getUser(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateRole(Long id, String role) {
        AuthUser user = userRepository.findById(id);
        user.getAuthRoles().clear();
        AuthRole roleByName = authRoleRepository.findRoleByName(role);
        user.getAuthRoles().add(roleByName);
        userRepository.save(user);
    }

    /*public void saveGithubUser(Map<String, Object> attributes) {
        String id = attributes.get("id").toString();
        BigInteger username = Util.getInstance().convertToBigInteger(id);
        System.out.println(username);
//        String email = (String) attributes.get("email");
//        String password = "the.Strongest.Password@Ever9";
//        String firstName = (String) attributes.get("name");
//        String lastName = null;
//        String[] strings = firstName.split(" ");

        for (Map.Entry<String, Object> stringObjectEntry : attributes.entrySet()) {
            System.out.println(stringObjectEntry.getKey() + " : " + stringObjectEntry.getValue());
        }

//        if (strings.length == 2) {
//            firstName = strings[0];
//            lastName = strings[1];
//        } else firstName = strings[0];

//        AuthUser user = AuthUser.builder()
//                .username(username)
//                .password(passwordEncoder.encode(password))
//                .email(email)
//                .firstName(firstName)
//                .lastName(lastName)
//                .status(Status.ACTIVE)
//                .build();
//        userRepository.save(user);
    }*/


    public void saveUserContent(UserContent userContent) {
        userContentRepository.save(userContent);
    }

    public List<Vocabulary> mapRequestToVocabularyList(HttpServletRequest request, Content content, AuthUser authUser) {
        List<Vocabulary> vocabularyList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            vocabularyList.add(mapVocabulary(request, i, authUser, content));
        }
        String[] uzbekWords = request.getParameterValues("uzbekWord");
        String[] englishWords = request.getParameterValues("englishWord");
        String[] definitions = request.getParameterValues("definition");
        if (uzbekWords == null || englishWords == null ||
                uzbekWords.length == 0 ||
                englishWords.length == 0 ||
                uzbekWords.length != englishWords.length
        ) {
            throw new RuntimeException("Please fill all fields");
        }
        for (int i = 0; i < uzbekWords.length; i++) {
            Vocabulary vocabulary = Vocabulary.builder()
                    .story(content)
                    .authUser(authUser)
                    .word(englishWords[i])
                    .translation(uzbekWords[i])
                    .definition(Objects.requireNonNullElse(definitions[i], ""))
                    .build();
            vocabularyList.add(vocabulary);
        }
        return vocabularyList;
    }

    private Vocabulary mapVocabulary(HttpServletRequest request, int i, AuthUser authUser, Content content) {
        return Vocabulary.builder()
                .word(request.getParameter("word" + i))
                .translation(request.getParameter("translation" + i))
                .definition(request.getParameter("definition" + i))
                .story(content)
                .authUser(authUser)
                .build();
    }

    public void addVocabularyList(List<Vocabulary> vocabularies) {
        vocabularyRepository.saveAll(vocabularies);
    }

    public List<Vocabulary> getVocabularies(long id, AuthUser authUser) {
        return vocabularyRepository.findAllByStoryIdAndAuthUserAndDeleted(id, authUser, false);
    }

    public Optional<Vocabulary> getVocabulary(long vocabId) {

        return vocabularyRepository.findById(vocabId);
    }

    public void updateVocabulary(Vocabulary vocabulary) {
        vocabularyRepository.save(vocabulary);
    }

    public void deleteVocabulary(@NonNull Long userId, @NonNull Vocabulary vocabulary) {
        vocabulary.setDeleted(true);
        vocabularyRepository.setAsDelete(userId, vocabulary.getId());
    }

    public void mapAndUpdate(HttpServletRequest request, Vocabulary vocabulary) {
        vocabulary.setWord(Objects.requireNonNullElse(request.getParameter("word"), vocabulary.getWord()));
        vocabulary.setTranslation(Objects.requireNonNullElse(request.getParameter("translation"), vocabulary.getTranslation()));
        vocabulary.setDefinition(Objects.requireNonNullElse(request.getParameter("definition"), vocabulary.getDefinition()));
        vocabularyRepository.updateVocabulary(vocabulary.getWord(), vocabulary.getTranslation(), vocabulary.getDefinition(), vocabulary.getId());
    }

    public CompletionStage<Object> getAllQuestions(Long id) {
        return CompletableFuture.supplyAsync(() -> questionRepository.findAllByContentId(id));
    }

    public CompletionStage<Object> getAllOptions(Object questions) {
        return CompletableFuture.supplyAsync(() -> {
            List<Questions> questionsList = (List<Questions>) questions;
            List<Options> options = new ArrayList<>();
            for (Questions question : questionsList) {
                options.addAll(optionRepository.findAllByQuestionId(question.getId()));
            }
            return options;
        });
    }

    public void update(AuthUser user) {
        userRepository.save(user);
    }

    public List<String> getBlockedUsers() {
        List<AuthUser> authUserList = userRepository.findAllByStatus(Status.BLOCKED);
        List<String> blockedUsers = new ArrayList<>();
        for (AuthUser authUser : authUserList) {
            blockedUsers.add(authUser.getUsername());
        }
        return blockedUsers;
    }

    public boolean isUserBlocked(String username){
        return userRepository.findByUsername(username).getStatus().equals(Status.BLOCKED);
    }

    public void updateUser(UpdateUserDTO dto) {
        AuthUser user = userRepository.findById(Long.valueOf(dto.id()));
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setUsername(dto.username());
        user.setBirthDate(dto.birthDate());
        userRepository.save(user);
    }

    public List<Levels> getAllLevels() {
        Levels[] levels = Levels.values();
        List<Levels> levelsList = Arrays.stream(levels).toList();
        return levelsList;
    }

    public String getSessionId(Long userId, HttpServletRequest request) {
        // retrieve the session ID associated with the user's session
        for (Object principal : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            if (principal instanceof AuthUser user) {
                if (user.getId().equals(userId)) {
                    HttpSession session = request.getSession(false);
                    if (session != null && user.getUsername().equals(session.getAttribute("username"))) {
                        return session.getId();
                    }
                }
            }
        }
        return null;
    }
}