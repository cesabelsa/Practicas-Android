package com.example.consumoapp.usuarios.data;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Utilidades para crear y verificar contraseñas con PBKDF2 + salt aleatorio. */
public final class PasswordUtils {
    private static final int ITERACIONES = 120000;
    private static final int LONGITUD_BITS = 256;
    private static final int BYTES_SALT = 16;

    private PasswordUtils() {}

    public static Credenciales crear(String password) {
        byte[] salt = new byte[BYTES_SALT];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivar(password, salt);
        return new Credenciales(
                Base64.encodeToString(hash, Base64.NO_WRAP),
                Base64.encodeToString(salt, Base64.NO_WRAP)
        );
    }

    public static boolean verificar(String password, String hashBase64, String saltBase64) {
        try {
            byte[] esperado = Base64.decode(hashBase64, Base64.NO_WRAP);
            byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
            byte[] calculado = derivar(password, salt);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derivar(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACIONES, LONGITUD_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo proteger la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static final class Credenciales {
        public final String hash;
        public final String salt;
        public Credenciales(String hash, String salt) { this.hash = hash; this.salt = salt; }
    }
}
