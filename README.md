# 🎙 Wavelet Recording App

A modern and lightweight *audio recording* app for Android that allows users to record voice notes with a clean UI and efficient file storage. Perfect for capturing quick audio memos, ideas, or interviews on the go.

⭐ If you like this project, please give it a *star* to show your support!

---

## ✨ Features

- 🎤 Record high-quality audio  
- 💾 Save recordings locally with unique filenames  
- 📂 Browse and manage recordings  
- 🧼 Clean and simple user interface  
- 🕹 Easy controls: Record, Pause, Stop  
- 🔊 Playback functionality for saved recordings  

---

## 🛠 Built With

- *Java*  
- *XML*  
- *Android Studio*  
- MediaRecorder for audio capture  
- MediaPlayer for audio playback  
- File API for saving and managing audio  
- RecyclerView for listing recordings  
- *Material Design* components  

---

## 🔧 Logic Used

### 🎙 Recording Audio
```java
mediaRecorder = new MediaRecorder();
mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
mediaRecorder.setOutputFile(filePath);
mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
mediaRecorder.prepare();
mediaRecorder.start();
