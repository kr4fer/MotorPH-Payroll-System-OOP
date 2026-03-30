<h1>📄 MotorPH Payroll System - Foxtrot Version</h1>
<p><strong>Course: MO-IT110 – Object-Oriented Programming</strong>


Group 28 – Terminal Assessment </p>

<hr>

<h3>👥 The Developers (Group 28)</h3>
<ul>
<li><strong>Anton Roger Galfo</strong> – Service &amp; DAO Layer, Authentication Logic, RBAC</li>
<li><strong>Sunny Eljohn Lico</strong> – System Architecture, IT Support &amp; Ticket Management</li>
<li><strong>Carl Justine Pontanilla</strong> – Employee Management, Salary Computation Pipeline</li>
<li><strong>Isidro Romano</strong> – Quality Assurance, Smoke Testing, &amp; Documentation</li>
</ul>

<hr>

<h3>🛠 Project Overview</h3>
<p>MotorPH Payroll System is a Java-based desktop application designed to streamline employee management, attendance tracking, and payroll processing. The <strong>Foxtrot Version</strong> introduces a robust layered architecture, role-based access control, and an integrated IT support ticketing system.</p>

<hr>

<h3>🔐 Access Credentials (UAT & Testing)</h3>
<p>For testing and evaluation purposes, the following accounts are pre-configured to demonstrate Role-Based Access Control (RBAC):</p>

<table border="1" cellpadding="5" cellspacing="0">
<thead>
<tr style="background-color: #f2f2f2;">
<th>Role</th>
<th>Username (un)</th>
<th>Password (pw)</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>System Admin</strong></td>
<td><code>admin</code></td>
<td><code>1234</code></td>
</tr>
<tr>
<td><strong>Human Resources</strong></td>
<td><code>hr</code></td>
<td><code>hr123</code></td>
</tr>
<tr>
<td><strong>Finance</strong></td>
<td><code>finance</code></td>
<td><code>finance123</code></td>
</tr>
<tr>
<td><strong>IT Specialist</strong></td>
<td><code>it_support</code></td>
<td><code>support123</code></td>
</tr>
<tr>
<td><strong>Regular Employee</strong></td>
<td><code>[Employee ID #]</code></td>
<td><code>[Last Name]</code></td>
</tr>
</tbody>
</table>
<p><em>Example Employee Login: un: <code>10001</code> | pw: <code>Garcia</code></em></p>

<hr>

<h3>🚀 Key System Enhancements</h3>
<p>The following updates focus on <strong>Data Integrity</strong> and <strong>Defensive Programming</strong> to ensure the system handles financial data accurately and securely:</p>
<ul>
<li><strong>Strict Numeric Filtering:</strong> Implemented a <code>DocumentFilter</code> that blocks alphabetic characters in real-time for Salary and Government ID fields.</li>
<li><strong>7-Digit Employee ID Cap:</strong> The system now strictly enforces a 7-character limit for Employee Numbers to maintain database consistency.</li>
<li><strong>Automated Currency Formatting:</strong> Using a <code>FocusListener</code>, the system automatically formats raw numbers into currency strings (e.g., <code>25,500.00</code>) once the user finishes typing.</li>
<li><strong>CSV Data Sanitization:</strong> The application "cleans" user input by stripping commas before saving to the CSV, ensuring compatibility with the <code>SalaryComputationPipeline</code>.</li>
</ul>

<hr>

<h3>🛠️ Feature Implementation Details</h3>
<table border="1" cellpadding="5" cellspacing="0">
<thead>
<tr style="background-color: #f2f2f2;">
<th>Field Category</th>
<th>Constraint / Logic</th>
<th>Validation Tool</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Employee #</strong></td>
<td>Max 7 Digits, Numbers Only</td>
<td><code>NumericDocumentFilter(7)</code></td>
</tr>
<tr>
<td><strong>Basic Salary</strong></td>
<td>Numbers + One Decimal + Auto-Commas</td>
<td><code>MoneyDocumentFilter</code> &amp; <code>FocusListener</code></td>
</tr>
<tr>
<td><strong>Government IDs</strong></td>
<td>Fixed Patterns (e.g., 00-0000000-0)</td>
<td><code>MaskFormatter</code></td>
</tr>
</tbody>
</table>

<hr>

<h3>✨ Key Features</h3>
<ul>
<li><strong>Role-Based Access Control (RBAC):</strong> Distinct dashboards for Staff (Admin/HR/Finance/IT) and regular Employees.</li>
<li><strong>Payroll Engine:</strong> Automated calculation of Gross Pay, Deductions (SSS, PhilHealth, Pag-IBIG, Tax), and Net Pay.</li>
<li><strong>Attendance Management:</strong> Comprehensive tracking and filtering of employee work hours.</li>
<li><strong>IT Support Module:</strong> A built-in ticketing system using UUIDs for tracking technical issues.</li>
<li><strong>Security:</strong> Account lockout mechanism (60-second cooldown after 3 failed login attempts).</li>
</ul>

<hr>

<h3>🏗 OOP Principles Applied</h3>
<ul>
<li><strong>Inheritance &amp; Abstraction:</strong> Used an abstract <code>Employee</code> class and <code>RegularEmployee</code> subclass to share core logic.</li>
<li><strong>Interfaces:</strong> Implemented the <code>IPayable</code> interface to ensure a standardized contract for all salary-related computations.</li>
<li><strong>Encapsulation:</strong> All model attributes are kept private and accessed via strict Getters and Setters.</li>
<li><strong>Polymorphism:</strong> Utilized polymorphic methods in the GUI to handle different employee types during computation.</li>
</ul>

<hr>

<h3>📂 Project Structure (Layered Architecture)</h3>
<ul>
<li><code>gui</code>: Handles the Swing-based user interface and event listeners.</li>
<li><code>service</code>: Contains business logic for payroll, leaves, and attendance.</li>
<li><code>dao</code>: Handles all file I/O operations with CSV data sources.</li>
<li><code>model</code>: Defines the core objects (Employee, Ticket, Deduction, etc.).</li>
</ul>

<hr>

<h3>🚀 Getting Started</h3>
<ol>
<li><strong>Prerequisites:</strong> Ensure you have Java JDK 11 or higher installed.</li>
<li><strong>Clone the Repo:</strong>


<code>git clone -b MotorPH_Payroll-Foxtrot-Version https://github.com/siejeihyung/MotorPH_OOP-Group-28.git</code>
</li>
</ol>
