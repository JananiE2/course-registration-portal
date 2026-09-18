package com.example.course_registration_portal.config;

import com.example.course_registration_portal.entity.*;
import com.example.course_registration_portal.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final RegistrationRepository registrationRepository;
    private final OptionalCourseContentRepository contentRepository;
    private final LessonProgressRepository progressRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CourseRepository courseRepository,
                           RegistrationRepository registrationRepository,
                           OptionalCourseContentRepository contentRepository,
                           LessonProgressRepository progressRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
        this.contentRepository = contentRepository;
        this.progressRepository = progressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedOrUpdateCourses();
        seedSampleRegistrations();
        seedInitialProgress();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setName("Admin Officer");
            admin.setEmail("admin@gateway.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("+91 98765 43210");
            admin.setRole("ADMIN");
            admin.setDepartment("Academic Administration");
            userRepository.save(admin);

            User student = new User();
            student.setName("Janani S");
            student.setEmail("student@gateway.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setPhone("+91 91234 56789");
            student.setRole("STUDENT");
            student.setDepartment("Computer Science & Engineering");
            userRepository.save(student);
        }
    }

    private void seedOrUpdateCourses() {
        List<CourseSeed> seeds = getCourseSeeds();

        for (CourseSeed seed : seeds) {
            Course course = courseRepository.findAll().stream()
                    .filter(c -> c.getCourseName().equalsIgnoreCase(seed.name))
                    .findFirst()
                    .orElse(null);

            if (course == null) {
                course = new Course();
                course.setCourseName(seed.name);
                course.setStatus("ACTIVE");
            }

            course.setCategory(seed.category);
            course.setDescription(seed.description);
            course.setDuration(seed.duration);
            course.setFee(seed.fee);
            course.setTrainerName(seed.trainerName);
            course.setTrainerEmail(seed.trainerEmail);
            course.setTrainerBio(seed.trainerBio);
            course.setClassSchedule(seed.classSchedule);
            course.setMeetingPlatform(seed.meetingPlatform);
            course.setMeetingLink(seed.meetingLink);
            course.setClassroomNotice(seed.classroomNotice);
            course.setMaterialsUrl(seed.materialsUrl);

            course = courseRepository.save(course);

            // Seed or update topics
            List<OptionalCourseContent> existingTopics = contentRepository.findByCourseIdOrderBySequenceNoAsc(course.getId());
            if (existingTopics.isEmpty()) {
                int seq = 1;
                for (TopicSeed ts : seed.topics) {
                    OptionalCourseContent occ = new OptionalCourseContent(
                            course,
                            ts.title,
                            ts.description,
                            seq++,
                            ts.learningObjectives,
                            ts.resourceLink,
                            ts.durationHours
                    );
                    contentRepository.save(occ);
                }
            } else {
                for (int i = 0; i < existingTopics.size() && i < seed.topics.size(); i++) {
                    OptionalCourseContent occ = existingTopics.get(i);
                    TopicSeed ts = seed.topics.get(i);
                    occ.setLearningObjectives(ts.learningObjectives);
                    occ.setResourceLink(ts.resourceLink);
                    occ.setDurationHours(ts.durationHours);
                    contentRepository.save(occ);
                }
            }
        }
    }

    private void seedSampleRegistrations() {
        if (registrationRepository.count() == 0) {
            User student = userRepository.findByEmail("student@gateway.com").orElse(null);
            if (student != null) {
                List<Course> courses = courseRepository.findAll();
                if (courses.size() >= 2) {
                    Registration r1 = new Registration();
                    r1.setUser(student);
                    r1.setCourse(courses.get(0)); // React JS with MongoDB
                    r1.setRegistrationDate(LocalDateTime.now().minusDays(4));
                    r1.setStatus("CONFIRMED");
                    r1.setRemarks("Application approved. Virtual classroom unlocked.");
                    registrationRepository.save(r1);

                    Registration r2 = new Registration();
                    r2.setUser(student);
                    r2.setCourse(courses.get(1)); // Python with DataScience
                    r2.setRegistrationDate(LocalDateTime.now().minusDays(1));
                    r2.setStatus("PENDING");
                    r2.setRemarks("Application submitted. Under review by course coordinator.");
                    registrationRepository.save(r2);
                }
            }
        }
    }

    private void seedInitialProgress() {
        User student = userRepository.findByEmail("student@gateway.com").orElse(null);
        if (student != null) {
            Course course = courseRepository.findAll().stream()
                    .filter(c -> c.getCourseName().contains("React JS"))
                    .findFirst()
                    .orElse(null);

            if (course != null) {
                List<OptionalCourseContent> topics = contentRepository.findByCourseIdOrderBySequenceNoAsc(course.getId());
                if (topics.size() >= 2 && progressRepository.findByUserIdAndCourseId(student.getId(), course.getId()).isEmpty()) {
                    // Mark first 2 modules completed
                    progressRepository.save(new LessonProgress(student, course, topics.get(0), true));
                    progressRepository.save(new LessonProgress(student, course, topics.get(1), true));
                }
            }
        }
    }

    private List<CourseSeed> getCourseSeeds() {
        return List.of(
            new CourseSeed(
                "React JS with MongoDB",
                "Web Technologies & Full Stack",
                "Master full-stack modern web development with React 19, JavaScript ES6+, Node.js, Express, and MongoDB Atlas database integration.",
                "8 Weeks",
                new BigDecimal("12000.00"),
                "Dr. R. Vignesh Kumar",
                "vignesh.kumar@gatewaysoftwaresolutions.com",
                "Lead Full-Stack Architect with 12+ years building enterprise SaaS and cloud architectures across Fortune 500 tech teams.",
                "Mon - Fri, 10:00 AM - 12:00 PM IST (Morning Batch)",
                "https://meet.google.com/gss-react-batch",
                "Google Meet",
                "Welcome to the React & MongoDB Batch! Live sessions start daily at 10:00 AM IST. Please install Node.js v20+ and VS Code before Session 1.",
                "https://github.com/gatewaysoftwaresolutions/react-mongodb-masterclass",
                List.of(
                    new TopicSeed("Modern React Fundamentals & JSX", "Understanding virtual DOM, JSX syntax rules, components hierarchy, props drilling, and rendering performance.", "• Master JSX expressions & conditional rendering\n• Build reusable functional components\n• Understand the React 19 Reconciliation pipeline", "https://react.dev/learn", "4 Hours"),
                    new TopicSeed("Hooks, State & Context API", "Deep dive into useState, useEffect, useMemo, useCallback, and global application state management with Context API.", "• Implement custom hooks for asynchronous fetching\n• Avoid unnecessary component re-renders\n• Manage authentication state globally", "https://react.dev/reference/react", "6 Hours"),
                    new TopicSeed("Express.js REST APIs", "Building scalable RESTful HTTP services with Express, routing middleware, error handlers, and JWT authentication.", "• Design production-ready API controllers\n• Secure endpoints using middleware tokens\n• Validate client payloads with Zod / Joi", "https://expressjs.com/", "5 Hours"),
                    new TopicSeed("MongoDB Schema Design & Mongoose", "Data modeling for NoSQL, embedding vs referencing, Mongoose ODM schemas, aggregations, and MongoDB Atlas indexing.", "• Design high-throughput MongoDB collections\n• Execute complex multi-stage aggregation pipelines\n• Connect Mongoose securely with connection pooling", "https://www.mongodb.com/docs/", "6 Hours"),
                    new TopicSeed("Full-Stack Deployment & Authentication", "Integrating front-end and back-end, configuring CORS, environment variables, CI/CD pipelines, and cloud hosting.", "• Deploy production React builds to Vercel/Cloudflare\n• Containerize Node.js server with Docker\n• Test end-to-end user workflows", "https://render.com/docs", "5 Hours")
                )
            ),
            new CourseSeed(
                "Python with DataScience",
                "Data Science & AI",
                "Comprehensive data science immersion covering Python programming, data manipulation with Pandas & NumPy, visualization with Matplotlib/Seaborn, and machine learning models with Scikit-Learn.",
                "10 Weeks",
                new BigDecimal("15000.00"),
                "Dr. Anita Sundaram",
                "anita.sundaram@gatewaysoftwaresolutions.com",
                "Principal Data Scientist with 10+ years in predictive analytics, ML pipeline architecture, and statistical research.",
                "Mon - Fri, 02:00 PM - 04:00 PM IST (Afternoon Batch)",
                "https://meet.google.com/gss-datascience-live",
                "Google Meet",
                "Live hands-on labs will use Jupyter Notebook and Google Colab. Datasets will be posted to the materials repository 24 hours prior.",
                "https://github.com/gatewaysoftwaresolutions/python-datascience-bootcamp",
                List.of(
                    new TopicSeed("Python for Data Analysis", "Vectorized calculations with NumPy arrays, mathematical operations, and high-performance computing.", "• Master n-dimensional array manipulation\n• Vectorized broadcasting and linear algebra\n• Performance benchmarking against pure Python", "https://numpy.org/doc/stable/", "4 Hours"),
                    new TopicSeed("Exploratory Data Analysis (EDA)", "Data cleaning, missing value imputation, outlier detection, and transformation using Pandas DataFrames.", "• Tidy messy real-world CSV and JSON datasets\n• GroupBy aggregations and pivot transformations\n• Feature engineering for predictive modeling", "https://pandas.pydata.org/", "6 Hours"),
                    new TopicSeed("Statistical Inference & Visualization", "Probability distributions, hypothesis testing, confidence intervals, and visualization with Seaborn.", "• Perform parametric and non-parametric statistical tests\n• Create publication-grade visualizations\n• Interpret correlation vs causation metrics", "https://seaborn.pydata.org/", "5 Hours"),
                    new TopicSeed("Supervised & Unsupervised ML", "Regression, classification trees, random forests, clustering algorithms, and model evaluation metrics.", "• Train and tune Scikit-Learn estimators\n• Prevent data leakage using Pipelines\n• Evaluate precision, recall, and ROC-AUC curves", "https://scikit-learn.org/", "8 Hours"),
                    new TopicSeed("Real-world Capstone Project", "End-to-end pipeline: data ingestion, automated cleaning, model training, evaluation, and REST API deployment.", "• Build an end-to-end ML model pipeline\n• Deploy prediction API using FastAPI\n• Present business findings with Streamlit", "https://fastapi.tiangolo.com/", "6 Hours")
                )
            ),
            new CourseSeed(
                "Python with Django",
                "Web Technologies & Full Stack",
                "Build robust, enterprise-grade web applications with Python and the powerful Django framework. Covers MVT architecture, ORM, Django REST framework, authentication, and cloud deployment.",
                "8 Weeks",
                new BigDecimal("13500.00"),
                "Karthik Narayanan",
                "karthik.n@gatewaysoftwaresolutions.com",
                "Senior Backend Consultant specializing in high-concurrency Python architectures and microservices.",
                "Mon - Fri, 06:00 PM - 08:00 PM IST (Evening Batch)",
                "https://meet.google.com/gss-django-batch",
                "Google Meet",
                "Next session focuses on Django ORM Optimization and QuerySet evaluation.",
                "https://github.com/gatewaysoftwaresolutions/django-mastery",
                List.of(
                    new TopicSeed("Django Framework Core & MVT", "Django architecture, request-response cycle, URL dispatching, views, and template engine.", "• Structure enterprise Django applications\n• Implement dynamic views and class-based views\n• Leverage Django template tags and inheritance", "https://docs.djangoproject.com/", "4 Hours"),
                    new TopicSeed("Database Models, ORM & Migrations", "Model definitions, relationships (OneToOne, ForeignKey, ManyToMany), and migration management.", "• Model complex database domains in Python\n• Optimize SQL queries using select_related\n• Execute database migrations safely", "https://docs.djangoproject.com/en/stable/topics/db/", "6 Hours"),
                    new TopicSeed("Django REST Framework (DRF)", "Serializers, ViewSets, API routers, pagination, and token authentication.", "• Build clean REST APIs for web & mobile clients\n• Implement custom permissions and throttles\n• Test API endpoints with automated test cases", "https://www.django-rest-framework.org/", "6 Hours"),
                    new TopicSeed("Authentication & Security", "User registration, password reset flows, CSRF protection, and CORS configuration.", "• Secure user auth with custom User models\n• Configure session cookies and JWT headers\n• Implement rate limiting and security headers", "https://docs.djangoproject.com/en/stable/topics/auth/", "5 Hours"),
                    new TopicSeed("Deploying Django on Cloud", "Production deployment using Gunicorn, Nginx, PostgreSQL, and AWS EC2 / Render.", "• Configure static files and WhiteNoise\n• Run Gunicorn workers behind reverse proxy\n• Monitor production logs and database health", "https://gunicorn.org/", "5 Hours")
                )
            ),
            new CourseSeed(
                "Flutter using Mobile App",
                "Mobile Development",
                "Cross-platform iOS and Android mobile app development using Google Flutter framework and Dart language.",
                "8 Weeks",
                new BigDecimal("14000.00"),
                "Pooja Chandran",
                "pooja.c@gatewaysoftwaresolutions.com",
                "Mobile App Lead with 8+ years publishing high-ranking apps on Google Play and Apple App Store.",
                "Mon - Fri, 11:00 AM - 01:00 PM IST",
                "https://meet.google.com/gss-flutter-live",
                "Google Meet",
                "Make sure Flutter SDK 3.x and Android Studio emulator are configured on your system.",
                "https://github.com/gatewaysoftwaresolutions/flutter-mobile-course",
                List.of(
                    new TopicSeed("Dart Language Essentials", "Dart syntax, null safety, object-oriented concepts, and async/await futures.", "• Master modern Dart 3 null-safe programming\n• Understand asynchronous streams and isolates\n• Write clean, testable Dart classes", "https://dart.dev/", "4 Hours"),
                    new TopicSeed("Flutter Widget Architecture", "Stateless vs Stateful widgets, layout composition, gestures, and animations.", "• Compose responsive layouts with Flex and Stack\n• Build intuitive user micro-animations\n• Handle device screen size adaptations", "https://flutter.dev/docs", "6 Hours"),
                    new TopicSeed("State Management (Provider & Riverpod)", "Predictable application state architectures, separation of UI and business logic.", "• Implement reactive state with Riverpod\n• Manage dependency injection cleanly\n• Avoid unnecessary widget tree rebuilds", "https://riverpod.dev/", "6 Hours"),
                    new TopicSeed("REST APIs & Firebase Integration", "Consuming HTTP endpoints, Firebase Cloud Firestore, Push Notifications, and Authentication.", "• Integrate Firebase Auth and Cloud Firestore\n• Cache API data locally with Hive / SQLite\n• Handle background notifications with FCM", "https://firebase.google.com/docs/flutter/setup", "6 Hours"),
                    new TopicSeed("Building & Publishing Apps", "App bundles, signing keys, Play Store release management, and TestFlight deployment.", "• Generate signed Android App Bundles (AAB)\n• Configure app icons and launch splash screens\n• Submit applications for store compliance", "https://docs.flutter.dev/deployment", "4 Hours")
                )
            ),
            new CourseSeed(
                "Embedded and IoT",
                "Embedded & Hardware",
                "Hardware interfacing, embedded C/C++ programming, microcontrollers (Arduino, ESP32, STM32), sensor networks, and IoT cloud platforms.",
                "10 Weeks",
                new BigDecimal("16000.00"),
                "Prof. K. Balaji",
                "balaji.k@gatewaysoftwaresolutions.com",
                "Embedded Systems Specialist with extensive experience in industrial automation and IoT sensor arrays.",
                "Mon - Fri, 03:00 PM - 05:00 PM IST",
                "https://meet.google.com/gss-embedded-iot",
                "Google Meet",
                "Simulation environments (Wokwi & Proteus) are provided for learners without physical boards.",
                "https://github.com/gatewaysoftwaresolutions/embedded-iot-hub",
                List.of(
                    new TopicSeed("Embedded C & Microcontroller Architecture", "Register-level programming, memory mapping, and microcontroller peripherals.", "• Write efficient embedded C for microcontrollers\n• Understand memory constraints and clock trees\n• Interface digital GPIO pins with debounce logic", "https://www.microchip.com/", "5 Hours"),
                    new TopicSeed("GPIO, Timers, ADC, and Interfacing", "Pulse-Width Modulation (PWM), analog-to-digital converters, and interrupt service routines.", "• Configure hardware hardware timers and interrupts\n• Sample analog sensors using ADC with calibration\n• Control actuators and motors with PWM", "https://docs.espressif.com/", "6 Hours"),
                    new TopicSeed("ESP32 & Wireless Communication", "Wi-Fi station and AP modes, Bluetooth Low Energy (BLE), and serial communication protocols (SPI, I2C, UART).", "• Interface I2C and SPI peripheral sensors\n• Transmit telemetry over Wi-Fi sockets\n• Configure low-power sleep modes for battery nodes", "https://www.espressif.com/en/products/socs/esp32", "6 Hours"),
                    new TopicSeed("MQTT, HTTP & IoT Cloud Dashboards", "Publish-subscribe IoT messaging with MQTT brokers (Mosquitto, AWS IoT Core) and dashboard UI.", "• Transmit sensor payloads using lightweight MQTT\n• Build real-time monitoring cloud dashboards\n• Set automated alerting thresholds via webhooks", "https://mqtt.org/", "6 Hours"),
                    new TopicSeed("Edge IoT Project Implementation", "End-to-end smart sensor deployment: environmental monitoring, edge data logging, and cloud synchronization.", "• Build complete smart energy / environment device\n• Implement OTA (Over-The-Air) firmware updates\n• Present edge sensor telemetry live", "https://wokwi.com/", "5 Hours")
                )
            ),
            new CourseSeed(
                "Cyber Security / Hacking",
                "Cyber Security",
                "Ethical hacking, penetration testing methodologies, network protocol analysis, OWASP Top 10 vulnerabilities, and security incident response.",
                "12 Weeks",
                new BigDecimal("18000.00"),
                "Suresh Madhavan (CEH / CISSP)",
                "suresh.m@gatewaysoftwaresolutions.com",
                "Certified Ethical Hacker & Offensive Security consultant for banking and fintech infrastructures.",
                "Mon - Fri, 07:00 PM - 09:00 PM IST",
                "https://meet.google.com/gss-cybersec-live",
                "Google Meet",
                "All testing takes place strictly in isolated virtual lab networks (Kali Linux and Metasploitable).",
                "https://github.com/gatewaysoftwaresolutions/cybersecurity-lab-notes",
                List.of(
                    new TopicSeed("Networking & Security Fundamentals", "OSI model, TCP/IP handshakes, packet capture with Wireshark, and firewall architectures.", "• Analyze raw packet captures with Wireshark\n• Identify anomalous network traffic signatures\n• Understand firewall rules and port forwarding", "https://www.wireshark.org/docs/", "6 Hours"),
                    new TopicSeed("Footprinting, Scanning & Reconnaissance", "Nmap network mapping, vulnerability assessment with OpenVAS, and OSINT gathering techniques.", "• Perform non-intrusive target reconnaissance\n• Map live hosts and listening service versions\n• Audit network services against CVE databases", "https://nmap.org/book/man.html", "6 Hours"),
                    new TopicSeed("Web Application Penetration Testing", "OWASP Top 10: SQL Injection, Cross-Site Scripting (XSS), CSRF, SSRF, and authentication bypasses.", "• Exploit and remediate SQL Injection bugs\n• Audit web applications using Burp Suite Proxy\n• Implement defence-in-depth secure headers", "https://owasp.org/www-project-top-ten/", "8 Hours"),
                    new TopicSeed("Cryptography & Secure Architecture", "Symmetric/Asymmetric encryption, PKI certificates, password hashing, and zero-trust security principles.", "• Understand AES, RSA, and modern curve ciphers\n• Implement proper salting and key derivation\n• Audit TLS configurations and cipher suites", "https://csrc.nist.gov/", "6 Hours"),
                    new TopicSeed("SOC & Incident Handling Basics", "Log analysis with SIEM (Splunk/Elastic), detection engineering, and incident response playbooks.", "• Correlate endpoint and authentication logs\n• Identify brute-force and credential stuffing\n• Draft structured security incident reports", "https://attack.mitre.org/", "6 Hours")
                )
            ),
            new CourseSeed(
                "R Tool and R Program",
                "Data Science & AI",
                "Statistical computing and graphics using R. Master data wrangling with tidyverse/dplyr and visualization with ggplot2.",
                "6 Weeks",
                new BigDecimal("11000.00"),
                "Dr. Anita Sundaram",
                "anita.sundaram@gatewaysoftwaresolutions.com",
                "Statistical consultant with 10+ years teaching biostatistics and predictive modeling.",
                "Mon - Fri, 04:00 PM - 06:00 PM IST",
                "https://meet.google.com/gss-r-stats-live",
                "Google Meet",
                "RStudio Desktop and tidyverse package installation guide available in course resources.",
                "https://github.com/gatewaysoftwaresolutions/r-programming-analytics",
                List.of(
                    new TopicSeed("R Syntax & Data Structures", "Vectors, factors, matrices, data frames, and vectorised mathematical expressions.", "• Master core R data structures\n• Write clean vectorised operations\n• Avoid sluggish iterative loops in R", "https://www.r-project.org/", "4 Hours"),
                    new TopicSeed("Data Wrangling with dplyr & tidyr", "Piping operations (%>%), filtering, selecting, mutating, and reshaping data.", "• Clean datasets using tidyverse principles\n• Group and summarize statistical aggregates\n• Reshape wide datasets to long format", "https://dplyr.tidyverse.org/", "5 Hours"),
                    new TopicSeed("Advanced Graphics with ggplot2", "The grammar of graphics, aesthetics mapping, geoms, facets, and custom themes.", "• Construct layered statistical charts\n• Customize scales, palettes, and typography\n• Export publication-ready vector plots", "https://ggplot2.tidyverse.org/", "5 Hours"),
                    new TopicSeed("Statistical Testing & Regression", "T-tests, ANOVA, linear regression modeling, and diagnostic residual checks.", "• Formulate and test statistical hypotheses\n• Fit and evaluate multivariate regression models\n• Check for multicollinearity and heteroskedasticity", "https://www.rdocumentation.org/", "5 Hours"),
                    new TopicSeed("R Markdown & Interactive Dashboards", "Reproducible data science reports, parameterised analyses, and interactive Shiny apps.", "• Generate automated PDF/HTML analytic briefs\n• Embed live interactive plots with htmlwidgets\n• Deploy lightweight data apps with Shiny", "https://rmarkdown.rstudio.com/", "5 Hours")
                )
            ),
            new CourseSeed(
                "Web Technologies",
                "Web Technologies & Full Stack",
                "Essential foundations of modern web engineering: HTML5, CSS3, Bootstrap 5, and JavaScript ES6+.",
                "8 Weeks",
                new BigDecimal("10000.00"),
                "S. Priya",
                "priya.s@gatewaysoftwaresolutions.com",
                "Senior UI/UX & Frontend Engineer with passion for accessible, high-performance web experiences.",
                "Mon - Fri, 09:00 AM - 11:00 AM IST",
                "https://meet.google.com/gss-webtech-live",
                "Google Meet",
                "Live coding sessions every morning. Code snippets pushed to GitHub daily.",
                "https://github.com/gatewaysoftwaresolutions/web-technologies-curriculum",
                List.of(
                    new TopicSeed("Semantic HTML5 & Modern Layouts", "Semantic document tags, accessible forms, audio/video elements, and SEO fundamentals.", "• Structure accessible document outlines\n• Utilize semantic landmarks for screen readers\n• Implement valid HTML5 form inputs and validation", "https://developer.mozilla.org/en-US/docs/Web/HTML", "4 Hours"),
                    new TopicSeed("CSS3 Flexbox, Grid & Animations", "Flexbox layouts, CSS Grid systems, custom properties (variables), and smooth transitions.", "• Master one-dimensional Flexbox layouts\n• Build two-dimensional responsive CSS Grids\n• Create performant CSS micro-animations", "https://developer.mozilla.org/en-US/docs/Web/CSS", "5 Hours"),
                    new TopicSeed("Bootstrap 5 Responsive Components", "Grid system, navbar, cards, modals, dropdowns, and utility-first styling classes.", "• Build responsive mobile-first layouts rapidly\n• Customize Bootstrap theme variables\n• Implement dynamic client dialogs and toasts", "https://getbootstrap.com/docs/5.3/", "4 Hours"),
                    new TopicSeed("JavaScript ES6+ & DOM Events", "Variables (let/const), arrow functions, destructuring, modules, and event delegation.", "• Manipulate DOM elements and style attributes\n• Manage event bubbling and delegation efficiently\n• Work with ES6 classes and modules", "https://javascript.info/", "6 Hours"),
                    new TopicSeed("Asynchronous JavaScript & Fetch API", "Promises, async/await, JSON parsing, error handling, and RESTful API consumption.", "• Fetch data from remote web services\n• Handle HTTP errors and network timeouts\n• Render dynamic datasets smoothly into the DOM", "https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API", "5 Hours")
                )
            ),
            new CourseSeed(
                "Python MySQL",
                "Database & Backend",
                "Relational database design, SQL querying, indexing, and Python database integration.",
                "6 Weeks",
                new BigDecimal("9500.00"),
                "Karthik Narayanan",
                "karthik.n@gatewaysoftwaresolutions.com",
                "Database Administrator & Python Specialist with 10+ years managing MySQL clusters.",
                "Mon - Fri, 05:00 PM - 07:00 PM IST",
                "https://meet.google.com/gss-python-mysql",
                "Google Meet",
                "Hands-on SQL schema designing and benchmark query profiling during each lab session.",
                "https://github.com/gatewaysoftwaresolutions/python-mysql-essentials",
                List.of(
                    new TopicSeed("Relational Database Modeling", "ER diagrams, normal forms (1NF through BCNF), and data integrity constraints.", "• Design normalized relational schemas\n• Enforce primary, foreign, and unique constraints\n• Eliminate data anomalies and redundancies", "https://dev.mysql.com/doc/", "4 Hours"),
                    new TopicSeed("Advanced SQL Queries & Transactions", "Complex inner/outer joins, subqueries, group by, indexing, and ACID transactions.", "• Write performant multi-table JOIN queries\n• Implement transaction commit and rollback blocks\n• Leverage B-Tree indexes for fast querying", "https://dev.mysql.com/doc/refman/8.0/en/sql-statements.html", "5 Hours"),
                    new TopicSeed("Python MySQL Connector & CRUD", "mysql-connector-python, connection pooling, parameterized queries, and exception handling.", "• Connect Python scripts securely to MySQL\n• Prevent SQL injection with parameterized inputs\n• Manage database connection pools under concurrency", "https://dev.mysql.com/doc/connector-python/en/", "5 Hours"),
                    new TopicSeed("Database Performance & Indexing", "EXPLAIN query plans, slow query logging, and storage engine choices (InnoDB).", "• Analyze EXPLAIN execution plans\n• Optimize composite indexes for query filters\n• Monitor query throughput and server bottlenecks", "https://dev.mysql.com/doc/refman/8.0/en/optimization.html", "5 Hours"),
                    new TopicSeed("Building Database-Backed Applications", "Creating end-to-end Python CLI and web services backed by persistent MySQL storage.", "• Encapsulate database logic in DAO layers\n• Implement automated backup and restore scripts\n• Build production CRUD application backend", "https://docs.python.org/3/library/sqlite3.html", "5 Hours")
                )
            ),
            new CourseSeed(
                "Java / J2EE Frameworks",
                "Enterprise Java",
                "Enterprise Java development: Core Java 21, Spring Boot, Spring MVC, Spring Data JPA, and Hibernate.",
                "12 Weeks",
                new BigDecimal("16500.00"),
                "Dr. R. Vignesh Kumar",
                "vignesh.kumar@gatewaysoftwaresolutions.com",
                "Enterprise Java Architect specializing in Spring Boot, cloud deployments, and transactional systems.",
                "Mon - Fri, 08:00 AM - 10:00 AM IST",
                "https://meet.google.com/gss-java-frameworks",
                "Google Meet",
                "Please ensure JDK 21+ and Maven are installed. Starter projects are hosted on GitHub.",
                "https://github.com/gatewaysoftwaresolutions/java-enterprise-frameworks",
                List.of(
                    new TopicSeed("Core Java & Modern Features", "Object-Oriented design patterns, collections framework, streams API, and records.", "• Master Java 21 features (virtual threads, records)\n• Write declarative code with Streams and Lambdas\n• Implement SOLID object-oriented principles", "https://docs.oracle.com/en/java/", "6 Hours"),
                    new TopicSeed("Spring Boot Architecture & DI", "Inversion of Control (IoC), Dependency Injection, autowiring, and component lifecycle.", "• Understand Spring's ApplicationContext\n• Configure Spring beans and profiles\n• Externalize configuration with properties/YAML", "https://spring.io/projects/spring-boot", "6 Hours"),
                    new TopicSeed("Hibernate & Spring Data JPA", "Entity mapping, associations, repository interfaces, and JPQL queries.", "• Map relational tables to JPA entities cleanly\n• Avoid N+1 query problems with EntityGraphs\n• Write custom queries with @Query and pagination", "https://spring.io/projects/spring-data-jpa", "8 Hours"),
                    new TopicSeed("Spring MVC & RESTful Services", "Controllers, request mappings, DTO validation, and exception handling.", "• Build structured Spring MVC web endpoints\n• Validate client input with Jakarta Validation\n• Handle errors cleanly with @ControllerAdvice", "https://docs.spring.io/spring-framework/reference/web/webmvc.html", "6 Hours"),
                    new TopicSeed("Microservices & Enterprise Architecture", "Spring Boot microservices, API gateways, database transactions, and cloud readiness.", "• Structure clean layered enterprise architectures\n• Manage distributed transactions with @Transactional\n• Package and deploy Spring Boot container images", "https://spring.io/guides", "6 Hours")
                )
            ),
            new CourseSeed(
                "Power BI and Tableau",
                "Business Intelligence",
                "Turn raw business data into actionable visual stories and high-impact executive dashboards.",
                "6 Weeks",
                new BigDecimal("12500.00"),
                "Meenakshi Sundaram",
                "meenakshi.s@gatewaysoftwaresolutions.com",
                "Senior Business Intelligence Architect with extensive consulting experience in retail & banking analytics.",
                "Mon - Fri, 06:00 PM - 08:00 PM IST",
                "https://meet.google.com/gss-bi-tableau",
                "Google Meet",
                "Sample enterprise sales and financial datasets provided for all hands-on exercises.",
                "https://github.com/gatewaysoftwaresolutions/powerbi-tableau-mastery",
                List.of(
                    new TopicSeed("Data Transformation with Power Query", "ETL processes, data types, merging, appending, and automated data cleaning steps.", "• Ingest multi-source operational data\n• Clean and transform records with Power Query\n• Automate recurring data preparation workflows", "https://learn.microsoft.com/en-us/power-query/", "4 Hours"),
                    new TopicSeed("Data Modeling & DAX Formulas", "Star schema relationships, calculated columns, measures, and time-intelligence DAX.", "• Design star schemas with fact and dimension tables\n• Write complex DAX measures (CALCULATE, FILTER)\n• Compute Year-over-Year (YoY) variance metrics", "https://dax.guide/", "5 Hours"),
                    new TopicSeed("Tableau Visual Storytelling", "Dimensions and measures, calculated fields, dual-axis charts, and visual best practices.", "• Build interactive Tableau visual sheets\n• Design intuitive dual-axis and map charts\n• Guide executives through coherent data stories", "https://help.tableau.com/", "5 Hours"),
                    new TopicSeed("Executive KPI Dashboards", "Card visuals, drill-through capabilities, slicers, and cross-filtering interactivity.", "• Compose high-impact executive summaries\n• Configure drill-through and bookmark navigation\n• Optimize dashboard rendering latency", "https://powerbi.microsoft.com/", "5 Hours"),
                    new TopicSeed("Publishing & Scheduled Refresh", "Power BI Service, Tableau Cloud, automated refresh gateways, and role-level security.", "• Publish interactive dashboards securely\n• Configure scheduled data gateway refresh\n• Enforce Role-Level Security (RLS) policies", "https://learn.microsoft.com/en-us/power-bi/", "4 Hours")
                )
            ),
            new CourseSeed(
                "Gen AI LLMs and Prototyping",
                "Data Science & AI",
                "State-of-the-art Generative AI: prompt engineering, LLM APIs, LangChain, RAG architectures, and fast prototyping.",
                "8 Weeks",
                new BigDecimal("20000.00"),
                "Dr. Arvind Krishnan",
                "arvind.k@gatewaysoftwaresolutions.com",
                "AI Research Scientist working on LLM alignment, autonomous agents, and enterprise RAG deployments.",
                "Mon - Fri, 08:00 PM - 10:00 PM IST (Night Owl Batch)",
                "https://meet.google.com/gss-genai-live",
                "Google Meet",
                "Students receive sandbox credits for OpenAI and open-source Hugging Face model inference.",
                "https://github.com/gatewaysoftwaresolutions/genai-llm-prototyping",
                List.of(
                    new TopicSeed("LLM Architecture & Prompt Engineering", "Transformer attention mechanisms, temperature, top-p, few-shot prompts, and chain-of-thought.", "• Understand transformer decoding mechanisms\n• Craft effective few-shot and CoT prompt patterns\n• Mitigate model hallucinations with constraints", "https://www.promptingguide.ai/", "5 Hours"),
                    new TopicSeed("Vector Databases & Semantic Embeddings", "Text embeddings, cosine similarity, ChromaDB, Pinecone, and high-dimensional indexing.", "• Generate semantic vector embeddings\n• Index large document stores in Chroma/Pinecone\n• Execute fast approximate nearest-neighbor queries", "https://docs.trychroma.com/", "6 Hours"),
                    new TopicSeed("LangChain & Agentic Workflows", "Chains, PromptTemplates, memory buffers, tools, and autonomous agent loops.", "• Compose modular multi-step LangChain pipelines\n• Integrate external search and calculator tools\n• Maintain conversational state across interactions", "https://python.langchain.com/", "6 Hours"),
                    new TopicSeed("Retrieval-Augmented Generation (RAG)", "Document chunking strategies, contextual retrieval, re-ranking, and citation generation.", "• Implement production naive and advanced RAG\n• Evaluate retrieval accuracy with RAGAS\n• Generate verifiable source-backed answers", "https://arxiv.org/abs/2005.11401", "7 Hours"),
                    new TopicSeed("Full-Stack AI Application Prototyping", "Building interactive AI interfaces using Streamlit, Next.js, and streaming FastAPI backends.", "• Stream token responses in real time with SSE\n• Build polished chat user interfaces\n• Deploy production-ready Generative AI microapps", "https://streamlit.io/", "6 Hours")
                )
            )
        );
    }

    private static class CourseSeed {
        String name;
        String category;
        String description;
        String duration;
        BigDecimal fee;
        String trainerName;
        String trainerEmail;
        String trainerBio;
        String classSchedule;
        String meetingLink;
        String meetingPlatform;
        String classroomNotice;
        String materialsUrl;
        List<TopicSeed> topics;

        CourseSeed(String name, String category, String description, String duration, BigDecimal fee,
                   String trainerName, String trainerEmail, String trainerBio, String classSchedule,
                   String meetingLink, String meetingPlatform, String classroomNotice, String materialsUrl,
                   List<TopicSeed> topics) {
            this.name = name;
            this.category = category;
            this.description = description;
            this.duration = duration;
            this.fee = fee;
            this.trainerName = trainerName;
            this.trainerEmail = trainerEmail;
            this.trainerBio = trainerBio;
            this.classSchedule = classSchedule;
            this.meetingLink = meetingLink;
            this.meetingPlatform = meetingPlatform;
            this.classroomNotice = classroomNotice;
            this.materialsUrl = materialsUrl;
            this.topics = topics;
        }
    }

    private static class TopicSeed {
        String title;
        String description;
        String learningObjectives;
        String resourceLink;
        String durationHours;

        TopicSeed(String title, String description, String learningObjectives, String resourceLink, String durationHours) {
            this.title = title;
            this.description = description;
            this.learningObjectives = learningObjectives;
            this.resourceLink = resourceLink;
            this.durationHours = durationHours;
        }
    }
}
