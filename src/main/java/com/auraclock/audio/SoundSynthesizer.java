package com.auraclock.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundSynthesizer {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static volatile boolean playing = false;

    public static synchronized void playAlarmSound() {
        if (playing) return;
        playing = true;
        executor.submit(() -> {
            try {
                float sampleRate = 44100f;
                // 16-bit, mono, signed, big-endian
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                // Play alarm sequence 3 times or until stopped
                for (int loop = 0; loop < 3 && playing; loop++) {
                    // Beep 1: C5 (523 Hz)
                    generateTone(line, 523.25, 150, 0.4);
                    sleep(50);
                    // Beep 2: E5 (659 Hz)
                    generateTone(line, 659.25, 150, 0.4);
                    sleep(50);
                    // Beep 3: G5 (784 Hz)
                    generateTone(line, 783.99, 150, 0.4);
                    sleep(50);
                    // Sustained chord: C5 + E5 + G5 + C6 (1046 Hz)
                    generateChord(line, new double[]{523.25, 659.25, 783.99, 1046.50}, 1500, 0.5);
                    sleep(500);
                }

                line.drain();
                line.stop();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                playing = false;
            }
        });
    }

    public static synchronized void stopAlarmSound() {
        playing = false;
    }

    private static void generateTone(SourceDataLine line, double freq, int durationMs, double volume) {
        int length = (int) (44100 * (durationMs / 1000.0));
        byte[] buffer = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            double time = i / 44100.0;
            // Fade-out to prevent popping
            double fade = 1.0;
            if (i > length - 200) {
                fade = (length - i) / 200.0;
            }
            double angle = 2.0 * Math.PI * freq * time;
            double sample = Math.sin(angle) * volume * fade;
            short val = (short) (sample * 32767);
            buffer[2 * i] = (byte) ((val >> 8) & 0xFF);
            buffer[2 * i + 1] = (byte) (val & 0xFF);
        }
        line.write(buffer, 0, buffer.length);
    }

    private static void generateChord(SourceDataLine line, double[] freqs, int durationMs, double volume) {
        int length = (int) (44100 * (durationMs / 1000.0));
        byte[] buffer = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            double time = i / 44100.0;
            // Exponential decay for bell-like ring-out
            double decay = Math.exp(-time * 2.2);
            double sample = 0;
            for (double freq : freqs) {
                sample += Math.sin(2.0 * Math.PI * freq * time);
            }
            sample = (sample / freqs.length) * volume * decay;
            short val = (short) (sample * 32767);
            buffer[2 * i] = (byte) ((val >> 8) & 0xFF);
            buffer[2 * i + 1] = (byte) (val & 0xFF);
        }
        line.write(buffer, 0, buffer.length);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
