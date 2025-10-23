# credit_simulator
Armendo Chandra Credit Simulator 

Aplikasi menggunakan Java dengan framework spring boot

🚀 Cara Menjalankan Aplikasi
Berikut langkah-langkah untuk menjalankan aplikasi:
- pada cmd gunakan syntax untuk pull images docker : docker pull ghcr.io/mendochandra/credit_simulator:latest
 jika menggunakan mac book bisa : docker run --platform linux/amd64 ghcr.io/mendochandra/credit_simulator:latest

- aplikasi ini bisa dijalankan dengan 2 metode yaitu
1. dengan menggunakan syntax pada cmd console kalian : docker run -it --env-file .env credit_simulator
2. dengan menggunakan syntax pada cmd console kalian : docker run -it credit_simulator file_inputs.txt 
   untuk nomor 2 ini menjalankan inputan dari file yang terdapat pada package java application

(OPTIONAL)Jika ingin membuild docker image bisa masuk ke cmd dan masuk ke directory javanya dan jalankan syntax docker build -t credit_simulator .

🖥️ Penggunaan Aplikasi
Setelah dijalankan, aplikasi akan menampilkan menu perintah di dalam console.
Menu ini memuat berbagai opsi simulasi kredit.
Gunakan angka atau perintah yang tersedia untuk memilih menu yang diinginkan, lalu ikuti instruksi yang muncul di console.

🧪 Untuk menjalankan unit Testingnya
Pastikan sudah berada di root project.
Jalankan perintah berikut:
./mvnw test
atau jika menggunakan Maven terinstall global:
mvn test
Hasil pengujian akan ditampilkan di console.

📂 Repository
Link GitHub: https://github.com/mendochandra/credit_simulator.git

Teknologi yang Digunakan
- Java 17
- Spring Boot
- Docker
- Maven
