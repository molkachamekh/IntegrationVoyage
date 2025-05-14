Voyage Project - README
Description
The Voyage project is a Java-based application aimed at managing travel-related data. It is organized into modules for users and voyages, each containing specific components like models, services, and controllers. The application is structured to handle user registration and management, voyage creation, and interactions between users and voyages, such as bookings and guides.

The project follows a modular approach with clear separation between backoffice and frontoffice functionalities for both users and voyages.

Project Structure
plaintext
Copier
Voyage (Root Directory)
│
├── .idea/ (IDE configuration files)
├── .mvn/ (Maven configuration files)
├── src/
│   └── main/
│       └── java/
│           └── com.example.voyage/
│               ├── user/ (User Module)
│               │   ├── backoffice/ (User Backoffice controllers, models, and services)
│               │   ├── frontoffice/ (User Frontoffice controllers and services)
│               └── voyage/ (Voyage Module)
│                   ├── backoffice/ (Voyage Backoffice controllers, model, services)
│                   ├── frontoffice/ (Voyage Frontoffice controllers)
├── resources/
│   └── com.example.voyage/
│       ├── user/ (Resources related to the User Module)
│       └── voyage/ (Resources related to the Voyage Module)
├── pom.xml (Maven build file)
└── README.md (This file)
Database Structure
1. User Module
The User module manages user information and their bookings.

Table: User
Field	Type	Description
user_id	INT	Primary Key, Auto-incremented
username	VARCHAR	Unique username for the user
password	VARCHAR	Password for the user
email	VARCHAR	Email address of the user
full_name	VARCHAR	Full name of the user
date_of_birth	DATE	Date of birth of the user
phone_number	VARCHAR	Contact phone number

Table: Booking
Field	Type	Description
booking_id	INT	Primary Key, Auto-incremented
user_id	INT	Foreign Key referencing User table
voyage_id	INT	Foreign Key referencing Voyage table
booking_date	DATE	Date when the booking was made
status	VARCHAR	Status of the booking (Confirmed, Cancelled, etc.)

2. Voyage Module
The Voyage module contains information about the available voyages and the associated guides.

Table: Voyage
Field	Type	Description
voyage_id	INT	Primary Key, Auto-incremented
destination	VARCHAR	The destination of the voyage
departure_date	DATE	Departure date of the voyage
return_date	DATE	Return date of the voyage
price	DECIMAL	Price of the voyage
description	TEXT	Detailed description of the voyage
image	BLOB	Image related to the voyage (optional)

Table: Guide
Field	Type	Description
guide_id	INT	Primary Key, Auto-incremented
voyage_id	INT	Foreign Key referencing Voyage table
guide_name	VARCHAR	Name of the guide
contact_info	VARCHAR	Contact details for the guide
languages	VARCHAR	Languages spoken by the guide

Features
User Management: Users can register, log in, and manage their profile information.

Voyage Management: Admins can create and manage voyages, including adding details like destination, dates, price, and description.

Booking System: Users can book voyages and manage their bookings.

Guide Assignment: Each voyage can have associated guides with details like name, contact info, and languages spoken.