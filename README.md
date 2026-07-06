# Teledentistry-Project

## Project Overview
This project is a Teledentistry mobile application

The purpose of this project is to provide a digital environment where patients and dentists can connect remotely to facilitate oral health consultations, triaging, and treatement monitoring without requiring an in-person clinic visit. 

## Tech Stack
1. Frontend/Languages: Java
2. UI Framework: Jetpack Compose
3. IDE: Android Studio
4. Backend/Database: MySQL

## Features
1. Virtual Consultations
2. Digital Intake & E-Signatures
3. Store-and-Forward Imaging
4. Electronic Prescriptions (e-Rx)
5. Remote Patient Monitoring
6. Treatment Planning & Billing
7. Patient Education Resource

## Getting Started
To use this repo, you will need to get a local copy up and running. Follow these steps.

### Prerequisites

  ```sh
  -Android Studio
  -No npm (Node Package Manager) used for this project 
  ```

1. Setup for GitHub Actions Test/Deployment automations
```sh
1. Create the directory Structure
mkdir -p .github/workflows

2. Write the workflow file
Name the file ci.yml
This file handles Continuous Integration (CI) by validating every pull request and Continuous Delivery (CD) by deploying only when code merges into the main branch.

name: CI/CD Pipeline

# 1. Define the triggering events
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  # 2. Continuous Integration Job (Build & Test)
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout Code
      uses: actions/checkout@v4

    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '20'

    - name: Install Dependencies
      run: npm ci

    - name: Run Linter and Tests
      run: |
        npm run lint
        npm test

  # 3. Continuous Deployment Job
  deploy:
    needs: build-and-test # Only run if the test job passes
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    runs-on: ubuntu-latest

    steps:
    - name: Checkout Code
      uses: actions/checkout@v4

    - name: Deploy to Cloud Provider
      env:
        DEPLOY_API_KEY: ${{ secrets.DEPLOY_API_KEY }}
      run: |
        echo "Connecting to hosting server..."
        # Replace this line with your actual cloud CLI or deployment script
        curl -X POST -H "Authorization: Bearer $DEPLOY_API_KEY" https://yourprovider.com


```



### Installation

1. Clone the repo:
   ```sh
   git clone https://github.com/ejacoby0-0/Teledentistry-Project
   cd Teledentistry-Project
   ```

 
