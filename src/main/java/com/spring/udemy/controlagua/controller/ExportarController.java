package com.spring.udemy.controlagua.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.spring.udemy.controlagua.model.Usuario;
import com.spring.udemy.controlagua.service.ControlAguaService;
import com.spring.udemy.controlagua.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/exportar")
public class ExportarController {

    private final ControlAguaService controlAguaService;
    private final SpringTemplateEngine templateEngine;
    private final UsuarioService usuarioService;

    public ExportarController(ControlAguaService controlAguaService, SpringTemplateEngine templateEngine, UsuarioService usuarioService) {
        this.controlAguaService = controlAguaService;
        this.templateEngine = templateEngine;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/pdf")
    public void exportarPDF(HttpServletResponse response) throws IOException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        // Simulamos datos
        Map<String, Object> datos = new HashMap<>();
        datos.put("controles", controlAguaService.controlAguaList());

        // Procesamos plantilla
        String html = procesarHtml("reportes/reporte-pdf", datos);

        // Generamos el PDF en un buffer
        exportarHtmlComoPdf(html, pdfStream);

        // Enviamos el PDF al navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=control_agua.pdf");
        response.setContentLength(pdfStream.size());

        // Copiamos al outputStream final
        OutputStream responseOutputStream = response.getOutputStream();
        pdfStream.writeTo(responseOutputStream);
        responseOutputStream.flush();
    }

    @GetMapping("/pdf/usuario/{id}")
    public void exportarPDFUsuario(HttpServletResponse response, @PathVariable Long id) throws IOException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        // Simulamos datos
        Map<String, Object> data = new HashMap<>();
        data.put("controles", controlAguaService.findByUsuarioIdExport(id));

        //Pasamos el logo del proyecto a la vista XD
        String logoBase64 = getLogoAsBase64();
        data.put("logoBase64", logoBase64);
        Usuario usuario = usuarioService.buscarUsuarioPorId(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        data.put("usuario", usuario);

        // Procesamos plantilla
        String html = procesarHtml("reportes/reporte-control-usuario-pdf", data);

        // Generamos el PDF en un buffer
        exportarHtmlComoPdf(html, pdfStream);

        //Creamos un nombre dinamico para el PDF :V
        String nombreArchivo = "control_agua_" + usuario.getNombre() + "_" + usuario.getApellido();


        // Enviamos el PDF al navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo + ".pdf");
        response.setContentLength(pdfStream.size());

        // Copiamos al outputStream final
        OutputStream responseOutputStream = response.getOutputStream();
        pdfStream.writeTo(responseOutputStream);
        responseOutputStream.flush();
    }

    public String procesarHtml(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        return templateEngine.process(templateName, context);
    }

    public void exportarHtmlComoPdf(String html, OutputStream outputStream) throws IOException {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);
        builder.run();
    }

    private String getLogoAsBase64() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("static/images/logofinal.png");
        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
        return Base64.getEncoder().encodeToString(bytes);
    }

}
