package com.smartlibrary.config;

import com.smartlibrary.entity.*;
import com.smartlibrary.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final UserInterestRepository userInterestRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           BookRepository bookRepository,
                           UserInterestRepository userInterestRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.userInterestRepository = userInterestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database dataset verification and initialization...");

        // 1. Seed Categories
        if (categoryRepository.count() == 0) {
            log.info("Seeding production category dataset...");
            categoryRepository.saveAll(List.of(
                Category.builder().name("Artificial Intelligence").description("Machine learning, neural networks, and AI ethics.").build(),
                Category.builder().name("Machine Learning").description("Predictive algorithms, deep learning, and statistical modeling.").build(),
                Category.builder().name("Computer Science").description("Algorithms, software architecture, and system design.").build(),
                Category.builder().name("Data Science").description("Data engineering, analytics, visualization, and big data.").build(),
                Category.builder().name("Philosophy & Cognitive Science").description("Mind, consciousness, logic, and epistemology.").build(),
                Category.builder().name("Science & Physics").description("Quantum mechanics, astrophysics, and theoretical physics.").build(),
                Category.builder().name("Business & Startup").description("Entrepreneurship, strategy, and corporate growth.").build(),
                Category.builder().name("Self Help & Psychology").description("Mindset, productivity, and behavioral psychology.").build(),
                Category.builder().name("Web Development").description("Full-stack frameworks, React, Node.js, and web systems.").build(),
                Category.builder().name("Cyber Security").description("Ethical hacking, network defense, and cryptography.").build()
            ));
        }

        // 2. Seed User Interests
        if (userInterestRepository.count() == 0) {
            log.info("Seeding user interest taxonomy...");
            userInterestRepository.saveAll(List.of(
                UserInterest.builder().interestName("Artificial Intelligence").build(),
                UserInterest.builder().interestName("Machine Learning").build(),
                UserInterest.builder().interestName("Quantum Computing").build(),
                UserInterest.builder().interestName("Distributed Systems").build(),
                UserInterest.builder().interestName("Philosophy of Mind").build(),
                UserInterest.builder().interestName("Cyber Security").build(),
                UserInterest.builder().interestName("Web Architecture").build(),
                UserInterest.builder().interestName("Data Engineering").build()
            ));
        }

        // 3. Seed Default Admin User
        if (userRepository.findByEmail("admin@library.com").isEmpty()) {
            userRepository.save(User.builder()
                .fullName("System Administrator")
                .email("admin@library.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build());
            log.info("Default admin account created.");
        } else {
            log.info("Default admin account already exists.");
        }

        // 4. Seed Default Standard User
        if (userRepository.count() == 0) {
            log.info("Seeding default student/researcher credentials...");
            userRepository.save(User.builder()
                .fullName("Alex Reinholt")
                .email("user@library.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER)
                .avatar("avatar1.png")
                .build());
        }

        // 5. Seed Real Book Dataset
        if (bookRepository.count() == 0) {
            log.info("Seeding comprehensive production book dataset into MySQL...");

            Category ai = categoryRepository.findByName("Artificial Intelligence").orElse(null);
            Category ml = categoryRepository.findByName("Machine Learning").orElse(null);
            Category cs = categoryRepository.findByName("Computer Science").orElse(null);
            Category ds = categoryRepository.findByName("Data Science").orElse(null);
            Category phil = categoryRepository.findByName("Philosophy & Cognitive Science").orElse(null);
            Category sci = categoryRepository.findByName("Science & Physics").orElse(null);
            Category biz = categoryRepository.findByName("Business & Startup").orElse(null);
            Category self = categoryRepository.findByName("Self Help & Psychology").orElse(null);
            Category web = categoryRepository.findByName("Web Development").orElse(null);
            Category sec = categoryRepository.findByName("Cyber Security").orElse(null);

            if (ai != null && cs != null) {
                bookRepository.saveAll(List.of(
                    Book.builder()
                        .title("Clean Code: A Handbook of Agile Software Craftsmanship")
                        .author("Robert C. Martin")
                        .category(cs)
                        .isbn("978-0132350884")
                        .language("English")
                        .publicationYear(2008)
                        .pages(464)
                        .rating(4.8)
                        .coverImage("https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&q=80&w=600")
                        .keywords("clean code, refactoring, agile, software architecture")
                        .description("Even bad code can function. But if code isn't clean, it can bring a development organization to its knees.")
                        .aiSummary("Essential handbook on writing readable, maintainable, and highly robust software code.")
                        .totalCopies(8)
                        .availableCopies(8)
                        .build(),
                    Book.builder()
                        .title("Atomic Habits: An Easy & Proven Way to Build Good Habits")
                        .author("James Clear")
                        .category(self != null ? self : cs)
                        .isbn("978-0735211292")
                        .language("English")
                        .publicationYear(2018)
                        .pages(320)
                        .rating(4.9)
                        .coverImage("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=600")
                        .keywords("habits, productivity, psychology, self improvement")
                        .description("Tiny Changes, Remarkable Results. A proven framework for improving every single day.")
                        .aiSummary("Actionable psychological principles to form positive daily habits and dismantle counterproductive behavior.")
                        .totalCopies(12)
                        .availableCopies(12)
                        .build(),
                    Book.builder()
                        .title("Deep Learning & Neural Architectures")
                        .author("Dr. Elena Rostova")
                        .category(ai)
                        .isbn("978-0123456789")
                        .language("English")
                        .publicationYear(2024)
                        .pages(540)
                        .rating(4.9)
                        .coverImage("https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&q=80&w=600")
                        .keywords("deep learning, neural networks, transformers, pyTorch")
                        .description("Comprehensive guide to modern deep learning architectures, attention mechanisms, and scalable AI model deployment.")
                        .aiSummary("Essential reading for AI researchers focusing on transformer architectures and high-dimensional vector embeddings.")
                        .totalCopies(6)
                        .availableCopies(6)
                        .build(),
                    Book.builder()
                        .title("Designing Data-Intensive Applications")
                        .author("Martin Kleppmann")
                        .category(cs)
                        .isbn("978-1449373320")
                        .language("English")
                        .publicationYear(2017)
                        .pages(616)
                        .rating(4.8)
                        .coverImage("https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=600")
                        .keywords("distributed systems, scalability, databases, fault tolerance")
                        .description("An invaluable guide to data storage, processing architectures, consistency models, and fault-tolerant system design.")
                        .aiSummary("Master key trade-offs in distributed storage, replication algorithms, and consensus protocols.")
                        .totalCopies(10)
                        .availableCopies(10)
                        .build(),
                    Book.builder()
                        .title("Quantum Computation & Quantum Information")
                        .author("Michael A. Nielsen & Isaac L. Chuang")
                        .category(sci != null ? sci : cs)
                        .isbn("978-1107002173")
                        .language("English")
                        .publicationYear(2010)
                        .pages(706)
                        .rating(4.7)
                        .coverImage("https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&q=80&w=600")
                        .keywords("quantum computing, qubits, quantum algorithms, physics")
                        .description("The definitive textbook on quantum computation, quantum circuits, error correction, and information theory.")
                        .aiSummary("Foundational text covering qubit mechanics, Shor's algorithm, Grover's search, and physical implementations.")
                        .totalCopies(4)
                        .availableCopies(4)
                        .build(),
                    Book.builder()
                        .title("Artificial Intelligence: A Modern Approach")
                        .author("Stuart Russell & Peter Norvig")
                        .category(ai)
                        .isbn("978-0134610993")
                        .language("English")
                        .publicationYear(2020)
                        .pages(1152)
                        .rating(4.9)
                        .coverImage("https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=600")
                        .keywords("artificial intelligence, search algorithms, multi-agent systems, probabilistic logic")
                        .description("The standard textbook in artificial intelligence covering intelligent agents, automated reasoning, knowledge representation, and machine learning.")
                        .aiSummary("The authoritative textbook bridging classic symbolic AI with modern statistical learning and reinforcement learning.")
                        .totalCopies(7)
                        .availableCopies(7)
                        .build(),
                    Book.builder()
                        .title("Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow")
                        .author("Aurélien Géron")
                        .category(ml != null ? ml : ai)
                        .isbn("978-1492032649")
                        .language("English")
                        .publicationYear(2019)
                        .pages(856)
                        .rating(4.8)
                        .coverImage("https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&q=80&w=600")
                        .keywords("machine learning, scikit-learn, tensorflow, keras, deep learning")
                        .description("Through concrete examples, minimal theory, and production-ready Python frameworks, this book helps you gain an intuitive understanding of concepts.")
                        .aiSummary("Practical guide covering linear regression, decision trees, random forests, and deep neural nets using Python.")
                        .totalCopies(9)
                        .availableCopies(9)
                        .build(),
                    Book.builder()
                        .title("Superintelligence: Paths, Dangers, Strategies")
                        .author("Nick Bostrom")
                        .category(phil != null ? phil : ai)
                        .isbn("978-0199678112")
                        .language("English")
                        .publicationYear(2014)
                        .pages(352)
                        .rating(4.6)
                        .coverImage("https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&q=80&w=600")
                        .keywords("superintelligence, AI alignment, existential risk, future of technology")
                        .description("Presents a profound analysis of what happens when machine intelligence surpasses human intelligence.")
                        .aiSummary("Explores existential risks of artificial superintelligence and strategies for strategic control and value alignment.")
                        .totalCopies(5)
                        .availableCopies(5)
                        .build(),
                    Book.builder()
                        .title("The Pragmatic Programmer: Your Journey To Mastery")
                        .author("Andrew Hunt & David Thomas")
                        .category(cs)
                        .isbn("978-0135957059")
                        .language("English")
                        .publicationYear(2019)
                        .pages(352)
                        .rating(4.8)
                        .coverImage("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&q=80&w=600")
                        .keywords("pragmatic programmer, software engineering, best practices, career development")
                        .description("Illustrates the best approaches and major pitfalls of software development regardless of language or paradigm.")
                        .aiSummary("Timeless wisdom on software craftsmanship, domain modeling, automation, and personal responsibility in engineering.")
                        .totalCopies(10)
                        .availableCopies(10)
                        .build(),
                    Book.builder()
                        .title("Zero to One: Notes on Startups, or How to Build the Future")
                        .author("Peter Thiel")
                        .category(biz != null ? biz : cs)
                        .isbn("978-0804139298")
                        .language("English")
                        .publicationYear(2014)
                        .pages(224)
                        .rating(4.7)
                        .coverImage("https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=600")
                        .keywords("startups, innovation, technology, business strategy, monopoly")
                        .description("The great secret of our time is that there are still uncharted frontiers to explore and new inventions to create.")
                        .aiSummary("Contrarian insights on technology startups, building monopolies through innovation, and zero-to-one technological leaps.")
                        .totalCopies(6)
                        .availableCopies(6)
                        .build()
                ));
            }
        }

        log.info("Production book dataset successfully verified and seeded.");
    }
}
