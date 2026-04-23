# 🚀 Expense Tracker CI/CD Pipeline

This project implements a complete **CI/CD pipeline** for an Expense Tracker application using **GitHub Actions**, **Docker**, and **AWS ECR**.

It automates building, testing, security scanning, and pushing Docker images to the cloud.

---

## 📌 Features

- Automated CI/CD using GitHub Actions  
- Docker image build for backend & frontend  
- Push images to AWS Elastic Container Registry (ECR)  
- Security scanning using Trivy  
- Backend unit testing with Maven  
- Artifact sharing between jobs  
- Scheduled pipeline execution  

---

## 🧱 Tech Stack

- Backend: Java 17, Spring Boot, Maven  
- Frontend: Node.js 20, Vite  
- CI/CD: GitHub Actions  
- Containerization: Docker  
- Cloud: AWS ECR  
- Security: Trivy  

---

## 🔄 Workflow Overview

```
Push / PR / Schedule
        ↓
 Build Backend & Frontend
        ↓
      Testing
        ↓
  Security Scan (Trivy)
        ↓
 Build Docker Images
        ↓
 Push to AWS ECR
        ↓
 Image Security Scan
```

---

## ⚙️ Workflow Triggers

- Push to `main`  
- Pull requests to `main`  
- Manual trigger (`workflow_dispatch`)  
- Scheduled runs (weekdays)

```yaml
schedule:
  - cron: "10 14 * * 1-5"
```

---

## 🧪 Jobs Breakdown

### Build Backend
```bash
mvn package -DskipTests
```

### Build Frontend
```bash
npm ci
npm run build
```

### Run Tests
```bash
mvn clean test
```

### Security Scan
- Filesystem scan using Trivy  
- Output stored as artifact  

### Build & Push to ECR
```bash
docker build -t <ECR_REGISTRY>/backend:latest .
docker push <ECR_REGISTRY>/backend:latest
```

### Image Scan
- Docker image scanning using Trivy  

---

## 🔐 Required GitHub Secrets

| Name | Description |
|------|------------|
| AWS_ACCESS_KEY_ID | AWS access key |
| AWS_SECRET_ACCESS_KEY | AWS secret key |

---

## 🌍 GitHub Variables

| Name | Description |
|------|------------|
| AWS_REGION | AWS region (e.g., us-east-1) |

---

## 📦 Artifacts

- Backend JAR file  
- Frontend build (`dist/`)  
- Trivy scan report  

---

## 🛡️ Security

- Filesystem vulnerability scanning  
- Docker image scanning  
- Non-blocking security checks  

---

## ⚡ Key Highlights

- Parallel frontend & backend builds  
- Job dependencies using `needs`  
- Environment-based deployment (`production`)  
- Image versioning using commit SHA  
- Scheduled CI runs  

---

## 🚧 Future Improvements

- Add SonarQube  
- Add Slack notifications  
- Deploy to AWS ECS  
- Fail pipeline on critical vulnerabilities  

---

## 👨‍💻 Author

Alok Raj

---

