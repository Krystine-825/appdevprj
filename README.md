- Java
- Spring boot
- Spring MVC
- Spring Security
- Spring Data (JPA)
- MySQL
- Thymeleaf
- Bootstrap

![Example Image](screenshots/1.png) <br>
![Example Image](screenshots/2-2.png) <br>
![Example Image](screenshots/3-3.png) <br>
![Example Image](screenshots/4-4.png) <br>
![Example Image](screenshots/7.png) <br>
![Example Image](screenshots/8.png) <br>




Dưới đây là **cấu trúc tầng (layered architecture)** điển hình cho hệ thống bạn đang triển khai:

---

## 🧱 **Tầng kiến trúc hệ thống: Expense Tracker + CloudSQL + ArgoCD + Cloudflare + Kubernetes**

```
┌──────────────────────────────────────────────┐
│                🌐 CLIENT/UI                  │
│  (Trình duyệt người dùng truy cập app qua    │
│   domain + HTTPS được bảo vệ bởi Cloudflare) │
└──────────────────────────────────────────────┘
                 │ HTTPS
                 ▼
┌──────────────────────────────────────────────┐
│              🌩️ CLOUDFLARE CDN               │
│ - Chuyển HTTP thành HTTPS                    │
│ - Caching + WAF + SSL Termination            │
└──────────────────────────────────────────────┘
                 │ HTTPS
                 ▼
┌──────────────────────────────────────────────┐
│             ☁️ LOAD BALANCER (GKE)            │
│ - Trỏ domain về IP public của Service LB     │
└──────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────┐
│       🔀 KUBERNETES SERVICE (LoadBalancer)    │
│ - Trỏ port 443 hoặc 80 về port 8080 của app  │
└──────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────┐
│           🚀 KUBERNETES POD (2 container)     │
│ - Container 1: Java Spring Boot App           │
│ - Container 2: cloud-sql-proxy                │
│ - Cùng chia sẻ localhost (127.0.0.1:3306)     │
└──────────────────────────────────────────────┘
                 │ JDBC
                 ▼
┌──────────────────────────────────────────────┐
│           🛢️ CLOUD SQL (MySQL)               │
│ - Quản lý database                          │
│ - Kết nối qua Cloud SQL Proxy               │
└──────────────────────────────────────────────┘
```

---

## ⚙️ Quản lý & Deploy

```
┌──────────────────────────────────────────────┐
│             🚀 ARGOCD GITOPS                  │
│ - Theo dõi Git repo chứa YAML/Helm           │
│ - Tự động sync xuống GKE                     │
└──────────────────────────────────────────────┘
```

---

## 📦 Git Repository (DevOps layer)

```
git@github.com:your-org/expenses-app-gitops.git
├── apps/
│   └── expenses-app/
│       ├── deployment.yaml
│       ├── service.yaml
│       ├── ingress.yaml (nếu dùng nginx)
├── helm/ (nếu dùng Helm)
├── kustomize/ (nếu dùng Kustomize)
└── README.md
```

---

## ✅ Tóm tắt tầng:

| Tầng           | Công nghệ          | Vai trò chính                        |
| -------------- | ------------------ | ------------------------------------ |
| Client         | Browser + Domain   | Giao diện người dùng                 |
| CDN/SSL        | Cloudflare         | HTTPS, SSL Termination, Caching, WAF |
| Load Balancer  | GKE LoadBalancer   | Phân phối traffic đến pod            |
| Service Layer  | Kubernetes Service | Định tuyến nội bộ                    |
| App Layer      | Java Spring Boot   | API backend                          |
| DB Proxy Layer | Cloud SQL Proxy    | Cầu nối tới Cloud SQL                |
| Database       | Google Cloud SQL   | MySQL lưu trữ dữ liệu                |
| CI/CD Layer    | ArgoCD + Git       | GitOps tự động hoá triển khai        |

---

💬 Bạn có thể muốn thêm tầng **Nginx Ingress Controller** nếu cần xử lý domain/path nâng cao trong cụm. Mình có thể hỗ trợ cấu hình nếu bạn muốn. Bạn đang muốn mở rộng tầng nào thêm không?






Dưới đây là **kiến trúc tổng thể hệ thống** của bạn, được trình bày trực quan và phân lớp rõ ràng theo luồng từ người dùng → frontend → backend → database → quản lý CI/CD:

---

## 🧭 **Sơ đồ kiến trúc tổng thể hệ thống Expense Tracker**

```
                                              +----------------------------+
                                              |        Git Repository      |
                                              |     (YAML / Helm / Kustom) |
                                              +-------------+--------------+
                                                            |
                                                            v
                                              +----------------------------+
                                              |          ArgoCD            |
                                              |     (GitOps CD tool)       |
                                              +-------------+--------------+
                                                            |
                                                            v
                                                    Kubernetes Cluster (GKE)
                                               ┌────────────────────────────┐
     Người dùng (Browser)                      |                            |
  +--------------------------------+           |                            |
  | https://expenses.example.com  |<----------►|  LoadBalancer Service      |
  +--------------------------------+           |      (type: LoadBalancer)  |
                                              /|\                           |
                                               |                            |
                    +--------------------------+----------------------------+
                    |                      |                          |
                    |                      |                          |
        ┌────────────────────┐  ┌────────────────────┐    ┌────────────────────┐
        |    Web Pod (App)   |  |    Web Pod (App)   |    |    Web Pod (App)   |    ...
        |  - Java SpringBoot |  |  - Java SpringBoot |    |  - Java SpringBoot |
        |  - cloudsql-proxy  |  |  - cloudsql-proxy  |    |  - cloudsql-proxy  |
        └────────────────────┘  └────────────────────┘    └────────────────────┘
                    |
                    v (localhost:3306)
        ┌────────────────────────────┐
        |   Cloud SQL Proxy Tunnel   |  🔒 Secure TCP over TLS
        └────────────────────────────┘
                    |
                    v
        ┌────────────────────────────┐
        |   Google Cloud SQL (MySQL) |
        |   - DB: expenses_tracker   |
        └────────────────────────────┘

                          ▲
                          |
          +--------------------------------+
          |    Cloud IAM Service Account   |
          | - Role: Cloud SQL Client       |
          | - Key mounted in Secret        |
          +--------------------------------+

```

---

## 🧱 **Tầng công nghệ chi tiết**

| Tầng                 | Thành phần      | Công nghệ                        | Vai trò                       |
| -------------------- | --------------- | -------------------------------- | ----------------------------- |
| **1. Giao diện**     | Người dùng      | Trình duyệt + Cloudflare + HTTPS | Truy cập qua domain bảo mật   |
| **2. CDN + SSL**     | Cloudflare      | DNS + HTTPS + SSL Termination    | Đảm bảo bảo mật & hiệu suất   |
| **3. Load Balancer** | GKE Service     | LoadBalancer Service             | Phân phối traffic đến pod     |
| **4. Ứng dụng**      | Pod             | Java Spring Boot                 | Backend API server            |
|                      | Container phụ   | cloudsql-proxy                   | Kết nối Cloud SQL an toàn     |
| **5. Cơ sở dữ liệu** | Cloud SQL       | MySQL                            | Lưu dữ liệu                   |
| **6. Bảo mật**       | IAM + Secret    | Service Account JSON             | Ủy quyền truy cập DB          |
| **7. CI/CD**         | ArgoCD          | GitOps                           | Tự động hoá triển khai từ Git |
| **8. Git repo**      | GitHub / GitLab | YAML / Helm                      | Lưu trữ cấu hình triển khai   |

---

## 🛡️ **Các điểm bảo mật quan trọng**

* Cloudflare bảo vệ domain khỏi DDoS.
* HTTPS được bật từ Cloudflare đến LB.
* Cloud SQL Proxy dùng Service Account để truy cập database.
* Secrets được lưu dưới dạng Kubernetes Secret.
* ArgoCD có thể gắn RBAC theo namespace / project.

---

Nếu bạn cần **sơ đồ hình ảnh SVG/PNG**, mình có thể tạo bản đồ kiến trúc hệ thống này trực quan để bạn dùng trong tài liệu hoặc thuyết trình. Bạn có muốn mình tạo bản đồ này không?
