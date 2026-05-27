package com.elderlycare.user.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * 二维码生成工具类
 */
public class QRCodeUtil {

    /**
     * 生成老人用户的二维码（Base64格式）
     */
    public static String generateElderQRCodeBase64(String qrCodeToken) {
        if (qrCodeToken == null || qrCodeToken.isEmpty()) {
            throw new IllegalArgumentException("二维码token不能为空");
        }

        QrConfig config = new QrConfig();
        config.setMargin(2);
        config.setWidth(300);
        config.setHeight(300);
        config.setForeColor(Color.BLACK.getRGB());
        config.setBackColor(Color.WHITE.getRGB());

        BufferedImage image = QrCodeUtil.generate(qrCodeToken, config);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            cn.hutool.core.img.ImgUtil.write(image, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encode(imageBytes);
        } finally {
            IoUtil.close(outputStream);
        }
    }
}
