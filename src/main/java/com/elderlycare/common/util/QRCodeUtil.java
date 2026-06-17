package com.elderlycare.common.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 二维码生成工具类
 */
public class QRCodeUtil {

    /**
     * 生成老人用户的二维码（Base64格式）
     * @param qrCodeToken
     * @return Base64编码的二维码图片
     */
    public static String generateElderQRCodeBase64(String qrCodeToken) {

        if (qrCodeToken == null || qrCodeToken.isEmpty()) {
            throw new IllegalArgumentException("二维码token不能为空");
        }

        // 生成唯一的二维码内容，包含老人ID和时间戳
        String content = qrCodeToken;

        // 配置二维码
        QrConfig config = new QrConfig();
        //config.setImg("D:/java/work/backend/src/main/resources/static/logo.png"); // 可选：添加logo
        config.setMargin(2);
        config.setWidth(300);
        config.setHeight(300);

        // 设置颜色
        config.setForeColor(Color.BLACK.getRGB());
        config.setBackColor(Color.WHITE.getRGB());

        // 生成二维码图片
        BufferedImage image = QrCodeUtil.generate(content, config);

        // 转换为Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            cn.hutool.core.img.ImgUtil.write(image, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encode(imageBytes);
        } finally {
            IoUtil.close(outputStream);
        }
    }

    /**
     * 生成简单的二维码内容
     * @param elderId 老人ID
     * @return 二维码文本内容
     */
    public static String generateElderQRCodeContent(Integer elderId) {
        if (elderId == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }
        return "ELDER_ID:" + elderId;
    }
}
