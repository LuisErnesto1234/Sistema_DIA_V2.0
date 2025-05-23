package com.spring.udemy.controlagua.controller;

import com.spring.udemy.controlagua.model.ControlAgua;
import com.spring.udemy.controlagua.service.ControlAguaService;
import com.spring.udemy.controlagua.service.PdfGeneratorService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/reporte/fecha")
public class FechasReportesController {

    private final ControlAguaService controlAguaService;
    private final PdfGeneratorService pdfGeneratorService;

    public FechasReportesController(ControlAguaService controlAguaService, PdfGeneratorService pdfGeneratorService) {
        this.controlAguaService = controlAguaService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @GetMapping
    public String mostrarVistaInicial(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // Obtener años disponibles para el select
        List<Integer> availableYears = controlAguaService.getAvailableYears();
        model.addAttribute("availableYears", availableYears);

        // Inicializar listas vacías por defecto
        List<Integer> availableMonths = Collections.emptyList();
        List<Integer> availableDays = Collections.emptyList();

        // Obtener meses disponibles si hay año seleccionado
        if (year != null) {
            availableMonths = controlAguaService.getAvailableMonths(year);

            // Obtener días disponibles si hay año y mes seleccionado
            if (month != null) {
                availableDays = controlAguaService.getAvailableDays(year, month);
            }
        }

        // Agregar atributos al modelo
        model.addAttribute("availableMonths", availableMonths);
        model.addAttribute("availableDays", availableDays);

        // Resto de tu lógica (filtrado, paginación, etc.)
        Page<ControlAgua> controles = controlAguaService.filterByDate(year, month, day,
                PageRequest.of(page, size, Sort.by("fechaRegistro").descending()));

        model.addAttribute("controles", controles);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedDay", day);
        model.addAttribute("paginaActual", page);

        return "reportes/filtrar-fecha";
    }

    @PostMapping
    public String filtrar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        redirectAttributes.addAttribute("day", day);
        return "redirect:/reporte/fecha";
    }

    @GetMapping("/generar-pdf")
    public ResponseEntity<byte[]> generarPdfFiltrado(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {

        try {
            // Obtener los datos filtrados (misma lógica que en el método original)
            List<ControlAgua> controles = controlAguaService.filterByDate(year, month, day,
                    Pageable.unpaged()).getContent(); // Sin paginación para el PDF

            // Preparar el contexto para Thymeleaf
            Context context = new Context();
            context.setVariable("controles", controles);
            context.setVariable("title", "Reporte de Control de Agua");
            context.setVariable("filtro", generarTextoFiltro(year, month, day));

            // Generar PDF
            byte[] pdfBytes = pdfGeneratorService.generatePdf("reporte-control-agua", context);

            // Configurar respuesta HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "control-agua-" + LocalDate.now() + ".pdf";
            headers.setContentDispositionFormData("filename", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error al generar PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    // Método auxiliar para generar texto del filtro aplicado
    private String generarTextoFiltro(Integer year, Integer month, Integer day) {
        StringBuilder filtro = new StringBuilder("Filtro aplicado: ");

        if (year != null) {
            filtro.append("Año ").append(year);
            if (month != null) {
                filtro.append(", Mes ").append(month);
                if (day != null) {
                    filtro.append(", Día ").append(day);
                }
            }
        } else {
            filtro.append("Todos los registros");
        }

        return filtro.toString();
    }

    @GetMapping("/generar-excel")
    public void generarExcel(@RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month,
                             @RequestParam(required = false) Integer day,
                             HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=controles-" + year + "-" + month + "-" + day + ".xlsx");

            List<ControlAgua> controles = controlAguaService.filterByDate(year, month, day,
                    Pageable.unpaged()).getContent();

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Controles");

            // ---------------------------
            // Estilo negrita para título y filtro
            CellStyle boldStyle = workbook.createCellStyle();
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            // Estilo encabezado: fondo color y borde
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Estilo para datos: solo bordes
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // ---------------------------
            // Fila 1: Título
            Row row0 = sheet.createRow(0);
            Cell titleCell = row0.createCell(0);
            titleCell.setCellValue("Controles de agua");
            titleCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Fila 2: Filtro aplicado
            Row row1 = sheet.createRow(1);
            Cell filtroCell = row1.createCell(0);
            filtroCell.setCellValue(generarTextoFiltro(year, month, day));
            filtroCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            // Fila 4 (índice 3): Encabezados
            Row header = sheet.createRow(3);
            String[] encabezados = {"Usuario", "Fecha", "Hora Inicio", "Hora Fin", "Minutos Utilizados"};
            for (int i = 0; i < encabezados.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(encabezados[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de datos a partir de fila 5
            int rowNum = 4;
            for (ControlAgua c : controles) {
                Row row = sheet.createRow(rowNum++);

                // Usuario completo
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(c.getUsuario().getNombre() + " " + c.getUsuario().getApellido());
                cell0.setCellStyle(dataStyle);

                // Fecha
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(c.getFechaRegistro().toString());
                cell1.setCellStyle(dataStyle);

                // Hora Inicio
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(c.getHoraInicio().toString());
                cell2.setCellStyle(dataStyle);

                // Hora Fin
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(c.getHoraFin().toString());
                cell3.setCellStyle(dataStyle);

                // Minutos Utilizados como Integer ✅
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(c.getMinutosUtilizados());
                cell4.setCellStyle(dataStyle);
            }

// Auto-ajustar columnas
            for (int i = 0; i <= 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (IOException ex) {
            throw new IOException("Error al crear Excel: " + ex.getMessage());
        }
    }
}
