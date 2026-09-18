# Gateway Software Solutions — Course Registration Portal

A complete, production-grade Java Full-Stack web application engineered strictly adhering to the 12-page **Gateway Software Solutions (GSS) Course Registration Portal** specification.

---

## 🚀 Key Specifications & Tech Stack

- **Backend**: Java 21 / 26, Spring Boot 3.2.5, Spring MVC, Spring Data JPA, Hibernate, Jakarta Validation
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript ES6+, Bootstrap 5, Bootstrap Icons, Google Fonts (Outfit & Plus Jakarta Sans)
- **Database**: MySQL 8.0 / 9.x (`gss_course_db`, port 3306)
- **Authentication**: Application-level session authentication (No Spring Security filters, matching TRD specification)
- **Design System**: Modern Gateway purple aesthetic (`#6d28d9`, `#8b5cf6`, `#ede9fe`), glassmorphism, responsive cards, micro-animations

---

## 📚 Supplied 12 Course Catalogue (Auto-Seeded)

The portal automatically pre-populates all 12 official Gateway Software Solutions training programs:

1. **React JS with MongoDB** (Web Technologies & Full Stack — 8 Weeks)
2. **Python with DataScience** (Data Science & AI — 10 Weeks)
3. **Python with Django** (Web Technologies & Full Stack — 8 Weeks)
4. **Flutter using Mobile App** (Mobile Development — 8 Weeks)
5. **Embedded and IoT** (Embedded & Hardware — 10 Weeks)
6. **Cyber Security / Hacking** (Cyber Security — 12 Weeks)
7. **R Tool and R Program** (Data Science & AI — 6 Weeks)
8. **Web Technologies** (Web Technologies & Full Stack — 8 Weeks)
9. **Python MySQL** (Database & Backend — 6 Weeks)
10. **Java / J2EE Frameworks** (Enterprise Java — 12 Weeks)
11. **Power BI and Tableau** (Business Intelligence — 6 Weeks)
12. **Gen AI LLMs and Prototyping** (Data Science & AI — 8 Weeks)

---

## 🔐 Default Demo Accounts & 1-Click Quick Fill

The login page (`/login`) includes 1-click quick credentials autofill buttons:

| Role | Email | Password | Access & Capabilities |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@gateway.com` | `admin123` | Full Course CRUD, admission review, status approval/rejection |
| **Student** | `student@gateway.com` | `student123` | Course browsing, registration, unique ID receipts, dashboard tracking |

---

## 🗺️ Portal Navigation & Routes

| Route | View / Page | Description |
| :--- | :--- | :--- |
| `/` | Home | Hero introduction, 3-step explanation, featured courses, statistics |
| `/courses` | Course Catalogue | Complete searchable catalogue with domain category filter |
| `/courses/{id}` | Course Details | Detailed description, syllabus modules, fee, duration, registration CTA |
| `/register` | Student Registration | Account creation form with Jakarta validation |
| `/login` | Login | Session-based authentication with 1-click demo buttons |
| `/student/dashboard` | Student Dashboard | Track enrolled courses, approval statuses, and applicant stats |
| `/student/profile` | Student Profile | Edit personal contact info and update password |
| `/register-course/{id}` | Course Registration Form | Review application details and submit admission request |
| `/registration/{id}` | Confirmation Receipt | Official unique registration ID (`GSS-REG-xxx`) with print receipt |
| `/admin/dashboard` | Admin Dashboard | Metrics summary, recent registrations, quick action links |
| `/admin/courses` | Course Management | List, search, toggle ACTIVE/INACTIVE, edit, deactivate |
| `/admin/courses/new` | Create Course | Course creation form with validation |
| `/admin/courses/edit/{id}` | Edit Course | Modify existing course information |
| `/admin/registrations` | Registration Management | Filter applications by status/course and update admission state |
| `/logout` | Logout | Invalidate session and redirect to login |

---

## ⚙️ How to Run Locally

### 1. Database Setup
Ensure MySQL is running on port 3306 with user `root` and password `root`.
The application automatically creates and seeds `gss_course_db`.

### 2. Launch Spring Boot
In a terminal window from this directory:
```powershell
.\mvnw.cmd spring-boot:run
```

The portal will start on: **`http://localhost:8080`**
