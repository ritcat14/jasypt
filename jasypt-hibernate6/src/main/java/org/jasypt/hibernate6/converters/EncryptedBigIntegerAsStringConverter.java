package org.jasypt.hibernate6.converters;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate6.encryptor.HibernatePBEEncryptorRegistry;

import java.math.BigInteger;

public class EncryptedBigIntegerAsStringConverter extends JasyptConverter<BigInteger, String> {

    protected PBEStringEncryptor encryptor = null;

    public static ConverterConfig converterConfig;

    public static void setConverterConfig(final ConverterConfig converterConfig) {
        EncryptedBigIntegerAsStringConverter.converterConfig = converterConfig;
    }

    @Override
    protected void checkInitialized() {
        if (!this.initialized) {
            if (converterConfig == null) {
                throw new ConverterInitializationException("Converter config is null for EncryptedBigIntegerAsStringConverter");
            } else {

                if (converterConfig.useEncryptorName) {

                    final HibernatePBEEncryptorRegistry registry =
                            HibernatePBEEncryptorRegistry.getInstance();
                    final PBEStringEncryptor pbeEncryptor =
                            registry.getPBEStringEncryptor(converterConfig.getProperty(EncryptionParameters.ENCRYPTOR_NAME));
                    if (pbeEncryptor == null) {
                        throw new EncryptionInitializationException(
                                "No string encryptor registered for hibernate " +
                                        "with name \"" + converterConfig.getProperty(EncryptionParameters.ENCRYPTOR_NAME) + "\"");
                    }
                    this.encryptor = pbeEncryptor;

                } else {

                    final StandardPBEStringEncryptor newEncryptor = new StandardPBEStringEncryptor();

                    newEncryptor.setPassword(converterConfig.getProperty(EncryptionParameters.PASSWORD));

                    if (converterConfig.getProperty(EncryptionParameters.ALGORITHM) != null)
                        newEncryptor.setAlgorithm(converterConfig.getProperty(EncryptionParameters.ALGORITHM));

                    if (converterConfig.getProperty(EncryptionParameters.PROVIDER_NAME) != null)
                        newEncryptor.setProviderName(converterConfig.getProperty(EncryptionParameters.PROVIDER_NAME));

                    if (converterConfig.getProperty(EncryptionParameters.KEY_OBTENTION_ITERATIONS) != null)
                        newEncryptor.setKeyObtentionIterations(converterConfig.getProperty(EncryptionParameters.KEY_OBTENTION_ITERATIONS));

                    if (converterConfig.getProperty(EncryptionParameters.STRING_OUTPUT_TYPE) != null)
                        newEncryptor.setStringOutputType(converterConfig.getProperty(EncryptionParameters.STRING_OUTPUT_TYPE));

                    newEncryptor.initialize();

                    this.encryptor = newEncryptor;

                }
            }
            this.initialized = true;
        }
    }

    @Override
    public String convertToDatabaseColumn(BigInteger value) {
        checkInitialized();
        if (value == null) {
            return null;
        }
        return encryptor.encrypt(value.toString());
    }

    @Override
    public BigInteger convertToEntityAttribute(String s) {
        checkInitialized();
        if (s == null) {
            return null;
        }
        return new BigInteger(encryptor.decrypt(s));
    }
}
