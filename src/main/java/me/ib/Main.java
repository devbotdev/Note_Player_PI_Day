package me.ib;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.*;

public class Main {
    private static final double C = 130.81;
    private static final double Db = 138.59;
    private static final double D = 146.83;
    private static final double Eb = 155.56;
    private static final double E = 164.81;
    private static final double F = 174.61;
    private static final double Gb = 185;
    private static final double G = 196;
    private static final double Ab = 207.65;
    private static final double A = 220;
    private static final double Bb = 233.08;
    private static final double B = 246.94;
    private static final Map<String, int[]> MODES = new HashMap<>();

    static {
        MODES.put("major", new int[]{0, 2, 4, 5, 7, 9, 11, 12, 14});
        MODES.put("minor", new int[]{0, 2, 3, 5, 7, 8, 10, 12, 14});
        MODES.put("dorian", new int[]{0, 2, 3, 5, 7, 9, 10, 12, 14});
        MODES.put("phrygian", new int[]{0, 1, 3, 5, 7, 8, 10, 12, 13});
        MODES.put("lydian", new int[]{0, 2, 4, 6, 7, 9, 11, 12, 14});
        MODES.put("mixolydian", new int[]{0, 2, 4, 5, 7, 9, 10, 12, 14});
        MODES.put("locrian", new int[]{0, 1, 3, 5, 6, 8, 10, 12, 13});
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter root note (C, D, E, etc.): ");
        String rootNote = scanner.next();

        System.out.print("Enter mode (major, minor, dorian, phrygian, lydian, mixolydian, locrian): ");
        String modeChoice = scanner.next().toLowerCase();

        int[] intervals = MODES.getOrDefault(modeChoice, MODES.get("major"));
        double rootFrequency = getNoteFrequency(rootNote);
        double[] scaleFrequencies = generateScale(rootFrequency, intervals);

        System.out.print("Enter a number sequence: ");
        String input = scanner.next();
        scanner.close();

        int sampleRate = 44100;
        int durationMs = 500;

        List<byte[]> soundSequence = new ArrayList<>();

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                int degree = Character.getNumericValue(c) - 1;
                if (degree == -1) { // Rest note for '0'
                    System.out.println("Playing rest for digit: 0");
                    soundSequence.add(generateSilence(durationMs, sampleRate));
                } else if (degree >= 0 && degree < scaleFrequencies.length) {
                    double frequency = scaleFrequencies[degree];
                    System.out.println("Playing note for digit: " + (degree + 1) + " (" + frequency + " Hz)");
                    soundSequence.add(generateSineWave(frequency, durationMs, sampleRate));
                }
            }
        }

        playSound(mergeBuffers(soundSequence), sampleRate);
    }

    public static double getNoteFrequency(String note) {
        switch (note) {
            case "Db": return Db;
            case "D": return D;
            case "Eb": return Eb;
            case "E": return E;
            case "F": return F;
            case "Gb": return Gb;
            case "G": return G;
            case "Ab": return Ab;
            case "A": return A;
            case "Bb": return Bb;
            case "B": return B;
            default: return C;
        }
    }

    public static double[] generateScale(double rootFrequency, int[] intervals) {
        double[] scale = new double[intervals.length];
        for (int i = 0; i < intervals.length; i++) {
            scale[i] = rootFrequency * Math.pow(2, intervals[i] / 12.0);
        }
        return scale;
    }

    public static byte[] generateSineWave(double frequency, int durationMs, int sampleRate) {
        int totalSamples = (int) ((durationMs / 1000.0) * sampleRate);
        byte[] buffer = new byte[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double angle = 2.0 * Math.PI * i * frequency / sampleRate;
            buffer[i] = (byte) (Math.sin(angle) * 127);
        }

        return buffer;
    }

    public static byte[] generateSilence(int durationMs, int sampleRate) {
        int totalSamples = (int) ((durationMs / 1000.0) * sampleRate);
        return new byte[totalSamples];
    }

    public static byte[] mergeBuffers(List<byte[]> buffers) {
        int totalLength = buffers.stream().mapToInt(b -> b.length).sum();
        byte[] merged = new byte[totalLength];
        int pos = 0;
        for (byte[] buffer : buffers) {
            System.arraycopy(buffer, 0, merged, pos, buffer.length);
            pos += buffer.length;
        }
        return merged;
    }

    public static void playSound(byte[] buffer, int sampleRate) throws Exception {
        try {
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}