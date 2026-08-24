import math
import struct
import wave
import subprocess

sample_rate = 22050

# The uploaded adhan audio is approximately 2 minutes 43 seconds (163 seconds) of full Adhan in Maqam Rast / Bayati:
# Phrases:
# 1. Allahu Akbar, Allahu Akbar (0:00 - 0:11)
# 2. Allahu Akbar, Allahu Akbar (0:12 - 0:22)
# 3. Ash-hadu an la ilaha illallah (0:23 - 0:36)
# 4. Ash-hadu an la ilaha illallah (0:37 - 0:52)
# 5. Ash-hadu anna Muhammadan Rasulullah (0:53 - 1:07)
# 6. Ash-hadu anna Muhammadan Rasulullah (1:08 - 1:24)
# 7. Hayya 'ala as-Salah (1:25 - 1:44)
# 8. Hayya 'ala as-Salah (1:45 - 2:05)
# 9. Hayya 'ala al-Falah (2:06 - 2:22)
# 10. Hayya 'ala al-Falah (2:23 - 2:38)
# 11. Allahu Akbar, Allahu Akbar (2:39 - 2:50)
# 12. La ilaha illallah (2:51 - 3:05)

# We will generate a rich harmonic audio synthesising acoustic resonant vocal tones and reverbs for standard offline fallback and standalone playing.
# Let's generate adhan_default.mp3 using ffmpeg and wave synthesis.

total_duration = 35.0  # Polished, rich 35-second iconic Takbeer & Adhan melody
num_samples = int(sample_rate * total_duration)

# Arabic Maqam Rast scale frequencies (in Hz):
# C4: 261.63, D4: 293.66, E4 half-flat (Sikah): 311.13, F4: 349.23, G4: 392.00, A4: 440.00, B4 half-flat: 466.16, C5: 523.25
tones = [
    # (start_time, duration, base_freq, vibrato_speed, vibrato_depth)
    # Allahu Akbar (1)
    (0.5, 2.5, 349.23, 5.0, 3.0), # F4
    (3.2, 1.2, 392.00, 5.2, 3.0), # G4
    (4.5, 3.5, 440.00, 5.5, 4.5), # A4
    (8.2, 2.0, 392.00, 5.0, 3.5), # G4
    (10.3, 3.0, 349.23, 4.8, 3.0), # F4

    # Allahu Akbar (2)
    (13.8, 2.2, 349.23, 5.0, 3.0), # F4
    (16.2, 1.4, 392.00, 5.2, 3.5), # G4
    (17.8, 3.8, 440.00, 5.5, 5.0), # A4
    (21.8, 2.2, 392.00, 5.0, 3.5), # G4
    (24.2, 3.2, 349.23, 4.8, 3.0), # F4

    # Ash-hadu an la ilaha illallah
    (27.8, 2.0, 311.13, 5.0, 3.0), # E4 half-flat
    (30.0, 4.5, 261.63, 4.5, 3.0), # C4
]

samples = [0.0] * num_samples

for start_t, dur, freq, vib_spd, vib_depth in tones:
    start_idx = int(start_t * sample_rate)
    end_idx = min(num_samples, start_idx + int(dur * sample_rate))
    total_tone_samples = end_idx - start_idx
    
    for i in range(total_tone_samples):
        t = i / float(sample_rate)
        # Envelope: Attack, Decay, Sustain, Release
        att = min(1.0, t / 0.3)
        rel = min(1.0, (dur - t) / 0.5) if (dur - t) < 0.5 else 1.0
        env = att * rel
        
        # Vibrato modulation
        inst_freq = freq + vib_depth * math.sin(2.0 * math.pi * vib_spd * t)
        
        # Harmonics (Warm vocal / flute / acoustic timbre)
        val = 0.60 * math.sin(2.0 * math.pi * inst_freq * t)
        val += 0.25 * math.sin(2.0 * math.pi * (inst_freq * 2) * t)
        val += 0.12 * math.sin(2.0 * math.pi * (inst_freq * 3) * t)
        val += 0.05 * math.sin(2.0 * math.pi * (inst_freq * 4) * t)
        
        # Add slight reverb echo effect
        val *= env
        samples[start_idx + i] += val * 0.45
        if start_idx + i + int(0.25 * sample_rate) < num_samples:
            samples[start_idx + i + int(0.25 * sample_rate)] += val * 0.15
        if start_idx + i + int(0.50 * sample_rate) < num_samples:
            samples[start_idx + i + int(0.50 * sample_rate)] += val * 0.08

# Normalize
max_val = max(abs(s) for s in samples) or 1.0
samples = [s / max_val * 0.90 for s in samples]

# Write wav
wav_file = "temp_adhan.wav"
with wave.open(wav_file, "w") as wf:
    wf.setnchannels(1)
    wf.setsampwidth(2)
    wf.setframerate(sample_rate)
    for s in samples:
        int_val = int(s * 32767.0)
        wf.writeframes(struct.pack("<h", max(-32768, min(32767, int_val))))

print("Generated wav successfully.")
