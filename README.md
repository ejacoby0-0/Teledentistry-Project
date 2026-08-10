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
To use this repo, you will need to set up a local copy and get it running. Follow these steps.

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

name: Android CI/CD Pipeline

on:
  push:
    branches: [ "main", "develop" ]
  pull_request:
    branches: [ "main" ]

jobs:
  test_and_build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout Code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '17'
        cache: 'gradle' 

    - name: Grant Execute Permission
      run: chmod +x gradlew

    - name: Run Unit Tests
      run: ./gradlew test

    - name: Build Debug APK
      run: ./gradlew assembleDebug

    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk


```



### Installation

1. Clone the repo:
   ```sh
   git clone https://github.com/ejacoby0-0/Teledentistry-Project
   cd Teledentistry-Project
   ```

 
