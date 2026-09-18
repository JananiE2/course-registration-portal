package com.example.course_registration_portal.service;

import com.example.course_registration_portal.dto.AssessmentQuestion;
import com.example.course_registration_portal.entity.AssessmentSubmission;
import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.entity.User;
import com.example.course_registration_portal.repository.AssessmentSubmissionRepository;
import com.example.course_registration_portal.repository.CourseRepository;
import com.example.course_registration_portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AssessmentService {

    public static final int PASSING_SCORE_PERCENTAGE = 60;

    private final AssessmentSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LearningService learningService;

    public AssessmentService(AssessmentSubmissionRepository submissionRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository,
                             LearningService learningService) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.learningService = learningService;
    }

    public boolean hasPassedAssessment(Long userId, Long courseId) {
        return submissionRepository.existsByUserIdAndCourseIdAndPassedTrue(userId, courseId);
    }

    public Optional<AssessmentSubmission> getLatestSubmission(Long userId, Long courseId) {
        return submissionRepository.findTopByUserIdAndCourseIdOrderBySubmittedAtDesc(userId, courseId);
    }

    public List<AssessmentSubmission> getAllSubmissions(Long userId, Long courseId) {
        return submissionRepository.findByUserIdAndCourseIdOrderBySubmittedAtDesc(userId, courseId);
    }

    @Transactional
    public AssessmentSubmission evaluateAndSave(Long userId, Long courseId, Map<String, String> answers) {
        if (!learningService.hasConfirmedRegistration(userId, courseId)) {
            throw new IllegalStateException("You must have a confirmed registration to take the final assessment.");
        }
        if (!learningService.isCourseFullyCompleted(userId, courseId)) {
            throw new IllegalStateException("You must complete all curriculum modules before taking the final assessment.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<AssessmentQuestion> questions = getQuestionsForCourse(course);
        int totalQuestions = questions.size();
        int correctCount = 0;

        for (int i = 0; i < totalQuestions; i++) {
            AssessmentQuestion q = questions.get(i);
            String submittedAnsStr = answers.get("q_" + q.getId());
            if (submittedAnsStr != null) {
                try {
                    int submittedIndex = Integer.parseInt(submittedAnsStr);
                    if (submittedIndex == q.getCorrectOptionIndex()) {
                        correctCount++;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int scorePercentage = totalQuestions > 0 ? (correctCount * 100) / totalQuestions : 0;
        boolean passed = scorePercentage >= PASSING_SCORE_PERCENTAGE;
        int nextAttempt = (int) submissionRepository.countByUserIdAndCourseId(userId, courseId) + 1;

        AssessmentSubmission submission = new AssessmentSubmission(
                user, course, scorePercentage, correctCount, totalQuestions, passed, nextAttempt
        );

        return submissionRepository.save(submission);
    }

    public List<AssessmentQuestion> getQuestionsForCourse(Course course) {
        String name = course.getCourseName().toLowerCase();
        String category = course.getCategory() != null ? course.getCategory().toLowerCase() : "";

        if (name.contains("python") || category.contains("python")) {
            return getPythonQuestions();
        } else if (name.contains("react") || category.contains("web") || name.contains("frontend") || name.contains("fullstack")) {
            return getReactWebQuestions();
        } else if (name.contains("java") || name.contains("spring") || category.contains("java")) {
            return getJavaSpringQuestions();
        } else if (name.contains("flutter") || name.contains("mobile") || name.contains("android") || name.contains("ios")) {
            return getFlutterMobileQuestions();
        } else if (name.contains("data science") || name.contains("machine learning") || name.contains("ai") || category.contains("data")) {
            return getDataScienceAiQuestions();
        } else if (name.contains("cloud") || name.contains("devops") || name.contains("aws") || name.contains("docker")) {
            return getCloudDevOpsQuestions();
        } else if (name.contains("security") || name.contains("cyber") || name.contains("ethical")) {
            return getCybersecurityQuestions();
        } else {
            return getDefaultSoftwareEngineeringQuestions();
        }
    }

    private List<AssessmentQuestion> getPythonQuestions() {
        return List.of(
                new AssessmentQuestion(1, "Which of the following is an immutable built-in data type in Python?",
                        List.of("List", "Dictionary", "Tuple", "Set"), 2, "Tuples are immutable sequences once defined in Python."),
                new AssessmentQuestion(2, "In Python web frameworks like Django and Flask, which HTTP status code signifies a resource was successfully created?",
                        List.of("200 OK", "201 Created", "204 No Content", "301 Moved Permanently"), 1, "HTTP 201 Created indicates successful resource persistence."),
                new AssessmentQuestion(3, "What is the primary purpose of a virtual environment (venv) in Python development?",
                        List.of("To speed up script execution", "To isolate project dependencies and avoid version conflicts", "To compile Python bytecode to machine code", "To connect Python directly to a GPU"), 1, "Virtual environments isolate package dependencies per project."),
                new AssessmentQuestion(4, "Which Python construct is best used for handling resource cleanup automatically (e.g., closing file streams or DB connections)?",
                        List.of("while True loop", "with context manager statement", "eval() function", "lambda expression"), 1, "The 'with' statement guarantees proper cleanup via context managers."),
                new AssessmentQuestion(5, "In modern relational databases and Python ORMs, what mechanism guarantees atomic transactions obeying ACID principles?",
                        List.of("Database indexing", "Transaction commit and rollback boundaries", "Foreign key cascades", "Primary key autoincrement"), 1, "Transactions ensure that multi-step operations either fully commit or rollback atomically.")
        );
    }

    private List<AssessmentQuestion> getReactWebQuestions() {
        return List.of(
                new AssessmentQuestion(1, "In React, what hook is used to manage and trigger component re-rendering based on state updates?",
                        List.of("useEffect", "useRef", "useState", "useMemo"), 2, "useState provides local component state and updater functions."),
                new AssessmentQuestion(2, "Why does React utilize a Virtual DOM rather than directly manipulating the browser DOM on every state change?",
                        List.of("To eliminate the need for JavaScript in the browser", "To batch and calculate minimal DOM diffs, maximizing rendering performance", "To prevent CSS styling conflicts", "To store data offline in LocalStorage"), 1, "The Virtual DOM calculates efficient diffs before patching the real DOM."),
                new AssessmentQuestion(3, "In a RESTful architecture, which HTTP method is typically used to update an entire resource record?",
                        List.of("GET", "POST", "PUT", "DELETE"), 2, "PUT is used for full replacement/update of an existing resource."),
                new AssessmentQuestion(4, "What is the purpose of the dependency array in the React useEffect hook?",
                        List.of("To specify which CSS stylesheets to bundle", "To control when the side-effect function re-executes based on prop/state changes", "To import external npm packages dynamically", "To define URL route parameters"), 1, "The dependency array dictates when the effect re-runs."),
                new AssessmentQuestion(5, "Which security header or practice protects modern web applications from Cross-Site Scripting (XSS)?",
                        List.of("Content Security Policy (CSP) & context-aware input escaping", "Disabling HTTPS", "Using plain text cookie storage", "CORS wildcard '*' on all endpoints"), 0, "CSP and proper escaping prevent malicious script execution.")
        );
    }

    private List<AssessmentQuestion> getJavaSpringQuestions() {
        return List.of(
                new AssessmentQuestion(1, "In the Spring Framework, what fundamental design pattern is used to decouple object creation from object usage?",
                        List.of("Inversion of Control / Dependency Injection", "Singleton Pattern without containers", "Factory Boy Pattern", "Active Record Pattern"), 0, "IoC/DI allows Spring to manage component lifecycles and wire dependencies."),
                new AssessmentQuestion(2, "Which Spring annotation designates a class as a REST controller returning serialized JSON responses?",
                        List.of("@Controller", "@RestController", "@Service", "@Repository"), 1, "@RestController combines @Controller and @ResponseBody."),
                new AssessmentQuestion(3, "In Spring Data JPA, what interface provides standard CRUD operations without requiring manual SQL query implementation?",
                        List.of("EntityManagerDirect", "JpaRepository", "QuerydslBridge", "SqlTemplateRunner"), 1, "JpaRepository provides out-of-the-box CRUD and pagination methods."),
                new AssessmentQuestion(4, "What does the @Transactional annotation guarantee in Spring service operations?",
                        List.of("That the method runs asynchronously on a new thread", "That all database modifications within the method execute in an atomic transaction", "That the response is cached in memory for 1 hour", "That user input is automatically validated for email format"), 1, "@Transactional manages database transaction commits and rollbacks."),
                new AssessmentQuestion(5, "In Java 8+, which feature is used for functional style sequence processing on collections?",
                        List.of("Enumeration", "Stream API with filter/map/reduce", "Vector loops", "Synchronized blocks"), 1, "Streams allow declarative, functional operations on collections.")
        );
    }

    private List<AssessmentQuestion> getFlutterMobileQuestions() {
        return List.of(
                new AssessmentQuestion(1, "In Flutter, what is the key difference between a StatelessWidget and a StatefulWidget?",
                        List.of("StatelessWidget requires Android SDK, StatefulWidget works on iOS only", "StatelessWidget cannot change its internal state during its lifetime; StatefulWidget can rebuild with setState()", "StatelessWidget cannot have child widgets", "StatefulWidget cannot connect to REST APIs"), 1, "StatefulWidgets maintain mutable state over the widget's lifecycle."),
                new AssessmentQuestion(2, "Which file in a Flutter application defines project metadata, asset paths, and package dependencies?",
                        List.of("AndroidManifest.xml", "pubspec.yaml", "Podfile", "config.json"), 1, "pubspec.yaml is Flutter's package and asset configuration file."),
                new AssessmentQuestion(3, "What does BuildContext represent in Flutter's widget tree?",
                        List.of("The mobile device's battery level", "The locator/handle to the location of a widget in the widget tree hierarchy", "The remote server endpoint", "The device screen resolution settings"), 1, "BuildContext represents the location of a widget in the widget tree."),
                new AssessmentQuestion(4, "In Dart, which keywords are used to handle asynchronous operations cleanly?",
                        List.of("thread & join", "async & await", "yield & goto", "defer & panic"), 1, "async and await allow writing asynchronous Dart code in a readable style."),
                new AssessmentQuestion(5, "Which Flutter widget is used to create a scrollable, high-performance list of dynamically generated items?",
                        List.of("Column", "ListView.builder", "Stack", "Container"), 1, "ListView.builder lazily builds items on demand as they scroll into view.")
        );
    }

    private List<AssessmentQuestion> getDataScienceAiQuestions() {
        return List.of(
                new AssessmentQuestion(1, "In Machine Learning, what problem occurs when a model performs exceptionally well on training data but poorly on unseen test data?",
                        List.of("Underfitting", "Overfitting (high variance)", "Data leakage", "Gradient vanishing"), 1, "Overfitting means the model memorized noise in the training set."),
                new AssessmentQuestion(2, "Which Python library is the standard industry foundation for tabular data manipulation and DataFrame analysis?",
                        List.of("Pygame", "Pandas", "Flask", "Celery"), 1, "Pandas provides DataFrames and rich tabular analysis tools."),
                new AssessmentQuestion(3, "For an imbalanced classification problem (e.g. fraud detection), why is Accuracy alone a misleading metric?",
                        List.of("Because accuracy is only for continuous regression", "Because predicting the majority class exclusively yields high accuracy while missing minority cases", "Because accuracy requires GPU acceleration", "Because accuracy cannot exceed 50%"), 1, "Precision, Recall, and F1-score are necessary for imbalanced datasets."),
                new AssessmentQuestion(4, "What is the primary role of an activation function (e.g. ReLU) in an Artificial Neural Network?",
                        List.of("To reduce dataset size", "To introduce non-linearity, allowing the network to learn complex patterns", "To encrypt model weights during training", "To convert Python code to C++"), 1, "Without non-linear activations, deep networks reduce to a simple linear regression."),
                new AssessmentQuestion(5, "In supervised learning, what is the dataset partitioned into to evaluate generalization performance unbiasedly?",
                        List.of("Training, Validation, and Test sets", "Client and Server sets", "Encrypted and Decrypted sets", "Alpha and Beta sets"), 0, "Training/validation/testing split guarantees independent evaluation.")
        );
    }

    private List<AssessmentQuestion> getCloudDevOpsQuestions() {
        return List.of(
                new AssessmentQuestion(1, "What is the primary difference between a Docker container and a traditional Virtual Machine?",
                        List.of("Containers virtualize hardware and run guest OSs, VMs do not", "Containers share the host OS kernel and are lightweight, while VMs run full guest operating systems on a hypervisor", "Containers can only run on Linux, VMs only on Windows", "Containers require physical hardware dongles"), 1, "Containers package user space and share the host kernel for speed and efficiency."),
                new AssessmentQuestion(2, "In Kubernetes, what is the smallest deployable computing unit that can be created and managed?",
                        List.of("Cluster", "Pod", "Ingress", "Namespace"), 1, "A Pod encapsulates one or more co-located containers."),
                new AssessmentQuestion(3, "What does CI/CD stand for in modern DevOps practice?",
                        List.of("Continuous Integration and Continuous Delivery / Deployment", "Cloud Infrastructure and Centralized Data", "Core Interface and Client Device", "Computer Instruction and Command Driver"), 0, "CI/CD automates code testing, building, and deployment."),
                new AssessmentQuestion(4, "What is Infrastructure as Code (IaC) tool best known for declarative multi-cloud provisioning?",
                        List.of("Terraform", "Postman", "Photoshop", "Jira"), 0, "Terraform provisions cloud resources using declarative HCL configurations."),
                new AssessmentQuestion(5, "Which AWS service provides scalable object storage accessible over HTTP REST APIs?",
                        List.of("Amazon EC2", "Amazon S3", "Amazon RDS", "Amazon Route 53"), 1, "Amazon S3 (Simple Storage Service) is object storage for the cloud.")
        );
    }

    private List<AssessmentQuestion> getCybersecurityQuestions() {
        return List.of(
                new AssessmentQuestion(1, "Which defense technique is the most effective against SQL Injection vulnerabilities?",
                        List.of("Disabling database passwords", "Using parameterized queries / prepared statements", "Storing passwords in plain text", "Limiting web server RAM"), 1, "Prepared statements separate SQL code from user-supplied data."),
                new AssessmentQuestion(2, "What is the core principle of Zero Trust security architecture?",
                        List.of("Trust everyone inside the corporate network perimeter", "Never trust, always verify every request regardless of origin", "Do not use passwords anywhere", "Disable all firewalls for maximum speed"), 1, "Zero Trust requires continuous authentication and least-privilege verification."),
                new AssessmentQuestion(3, "In modern cryptographic password storage, what is added to passwords before hashing to prevent rainbow table attacks?",
                        List.of("A digital signature", "A cryptographic Salt", "A timestamp only", "A symmetric key"), 1, "A salt ensures unique hashes even for identical passwords."),
                new AssessmentQuestion(4, "What type of attack involves tricking users into revealing sensitive credentials through deceptive emails or web portals?",
                        List.of("Phishing", "DDoS attack", "Buffer overflow", "SYN flood"), 0, "Phishing relies on social engineering to steal credentials."),
                new AssessmentQuestion(5, "What protocol provides end-to-end encrypted communication over the web using TLS certificates?",
                        List.of("HTTP", "HTTPS", "FTP", "Telnet"), 1, "HTTPS encrypts HTTP traffic using TLS.")
        );
    }

    private List<AssessmentQuestion> getDefaultSoftwareEngineeringQuestions() {
        return List.of(
                new AssessmentQuestion(1, "In Git version control, which command creates a new branch and switches to it in one step?",
                        List.of("git commit -m 'new branch'", "git checkout -b <branch_name>", "git merge <branch_name>", "git push origin master"), 1, "git checkout -b creates and switches to the branch."),
                new AssessmentQuestion(2, "What does the 'S' in the SOLID principles of object-oriented design stand for?",
                        List.of("Static Binding Principle", "Single Responsibility Principle", "System Integration Principle", "Serial Execution Principle"), 1, "Single Responsibility: a class should have one, and only one, reason to change."),
                new AssessmentQuestion(3, "In software testing, what is the primary purpose of Unit Tests?",
                        List.of("To verify the end-to-end user experience in the browser", "To isolate and verify individual functions or components in isolation", "To test server network bandwidth", "To benchmark database disk speed"), 1, "Unit tests verify individual components in isolation."),
                new AssessmentQuestion(4, "Which HTTP response code indicates that an endpoint requires user authentication that was not provided?",
                        List.of("200 OK", "401 Unauthorized", "404 Not Found", "500 Internal Server Error"), 1, "401 indicates lack of valid authentication credentials."),
                new AssessmentQuestion(5, "In clean code and architecture, why is loose coupling between system layers beneficial?",
                        List.of("It makes modules easier to test, maintain, and change independently", "It eliminates the need for unit tests", "It compiles code directly to assembly language", "It reduces database table sizes"), 0, "Loose coupling promotes maintainability and testability.")
        );
    }
}
