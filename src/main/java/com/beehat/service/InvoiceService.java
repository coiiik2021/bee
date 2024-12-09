package com.beehat.service;

import com.beehat.entity.Invoice;
import com.beehat.entity.InvoiceDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import java.util.ArrayList;
import java.util.List;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.ByteArrayOutputStream;
@Service
public class InvoiceService {
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByInvoiceId(invoice.getId());
        document.setMargins(20, 20, 20, 20);

        // Header
        Paragraph header = new Paragraph("INVOICE")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        // Info Section (Invoice & Customer Information)
        document.add(new Paragraph("Invoice ID: " + invoice.getId())
                .setBold().setFontSize(12));
        document.add(new Paragraph("Date: " + invoice.getCreatedDate()));
        document.add(new Paragraph("Customer: " + invoice.getCustomer().getFullname()));
        document.add(new Paragraph("Email: " + invoice.getCustomer().getEmail()));
        document.add(new Paragraph("Address: " + invoice.getCustomer().getAddress()));
        document.add(new Paragraph("Phone: " + invoice.getCustomer().getPhone()));

        document.add(new Paragraph(" ")); // Blank line for spacing

        // Product Section
        Table productTable = new Table(UnitValue.createPercentArray(new float[]{2, 5, 2, 2, 2}))
                .useAllAvailableWidth();

        // Table Header
        productTable.addHeaderCell(new Cell().add(new Paragraph("No.").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Product").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Quantity").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Price").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));

        // Add product rows
        int index = 1;
            for (InvoiceDetail invoiceDetail : invoiceDetails) {
                productTable.addCell(new Cell().add(new Paragraph(String.valueOf(index++))));
                productTable.addCell(new Cell().add(new Paragraph(invoiceDetail.getProductDetail().getProduct().getName())));
                productTable.addCell(new Cell().add(new Paragraph(String.valueOf(invoiceDetail.getQuantity()))));
                productTable.addCell(new Cell().add(new Paragraph(String.valueOf(invoiceDetail.getProductDetail().getPrice()))));
                productTable.addCell(new Cell().add(new Paragraph(String.valueOf(invoiceDetail.getFinalPrice()))));
            }
        document.add(productTable);
        document.add(new Paragraph(" ")); // Blank line for spacing

        // Totals Section
        document.add(new Paragraph("Subtotal: " + invoice.getTotalPrice()).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Tax: " + invoice.getInvoiceStatus()).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Discount: " + invoice.getVoucherDiscount()).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Total Price: " + invoice.getTotalPrice()).setBold().setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Final Price: " + invoice.getFinalPrice()).setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT));

        document.add(new Paragraph(" ")); // Blank line for spacing

        // Terms and Conditions (TNC)
        Paragraph tncHeader = new Paragraph("Terms & Conditions")
                .setBold()
                .setFontSize(12);
        document.add(tncHeader);

        Paragraph tncContent = new Paragraph("1. Payment is due within 30 days.\n"
                + "2. Late payments may incur additional charges.\n"
                + "3. Goods sold are not returnable or exchangeable.\n"
                + "4. Other terms and conditions may apply as per our policy.")
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY);
        document.add(tncContent);

        // Footer
        Paragraph footer = new Paragraph("Thank you for your business!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginTop(30);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }


}
