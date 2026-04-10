package dev.erosende.secadapter.soap;

import dev.erosende.billaton.application.domain.model.VerifactuRecordDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class VerifactuXmlBuilder {

    private static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String NS_SUM = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd";
    private static final String NS_SF = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd";
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FECHA_HORA_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Value("${verifactu.sistema.id:01}")
    private String sistemaId;

    @Value("${verifactu.sistema.version:1.0.0}")
    private String sistemaVersion;

    @Value("${verifactu.productor.nif:}")
    private String productorNif;

    @Value("${verifactu.productor.nombre:}")
    private String productorNombre;

    public String buildAltaXml(VerifactuRecordDto record) {
        String fechaExp = record.getFechaExpedicion().format(FECHA_FORMAT);
        String fechaHora = record.getFechaHoraGenRegistro().format(FECHA_HORA_FORMAT);

        StringBuilder xml = new StringBuilder();
        xml.append("<soapenv:Envelope xmlns:soapenv=\"").append(NS_SOAP).append("\">");
        xml.append("<soapenv:Body>");
        xml.append("<sum:RegFactuSistemaFacturacion xmlns:sum=\"").append(NS_SUM).append("\" xmlns:sf=\"").append(NS_SF).append("\">");

        // Cabecera
        xml.append("<sum:Cabecera>");
        xml.append("<sf:ObligadoEmision>");
        xml.append("<sf:NombreRazon>").append(escapeXml(record.getIssuerName())).append("</sf:NombreRazon>");
        xml.append("<sf:NIF>").append(record.getIssuerNif()).append("</sf:NIF>");
        xml.append("</sf:ObligadoEmision>");
        xml.append("</sum:Cabecera>");

        // RegistroFactura > RegistroAlta
        xml.append("<sum:RegistroFactura>");
        xml.append("<sf:RegistroAlta>");

        xml.append("<sf:IDVersion>1.0</sf:IDVersion>");

        // IDFactura
        xml.append("<sf:IDFactura>");
        xml.append("<sf:IDEmisorFactura>").append(record.getIssuerNif()).append("</sf:IDEmisorFactura>");
        xml.append("<sf:NumSerieFactura>").append(escapeXml(record.getNumSerieFactura())).append("</sf:NumSerieFactura>");
        xml.append("<sf:FechaExpedicionFactura>").append(fechaExp).append("</sf:FechaExpedicionFactura>");
        xml.append("</sf:IDFactura>");

        xml.append("<sf:NombreRazonEmisor>").append(escapeXml(record.getIssuerName())).append("</sf:NombreRazonEmisor>");
        xml.append("<sf:TipoFactura>").append(record.getTipoFactura()).append("</sf:TipoFactura>");

        // Rectificativa fields
        if (record.getTipoRectificativa() != null) {
            xml.append("<sf:TipoRectificativa>").append(record.getTipoRectificativa()).append("</sf:TipoRectificativa>");
            if (record.getFacturaRectificadaNif() != null) {
                xml.append("<sf:FacturasRectificadas><sf:IDFacturaRectificada>");
                xml.append("<sf:IDEmisorFactura>").append(record.getFacturaRectificadaNif()).append("</sf:IDEmisorFactura>");
                xml.append("<sf:NumSerieFactura>").append(escapeXml(record.getFacturaRectificadaNumSerie())).append("</sf:NumSerieFactura>");
                xml.append("<sf:FechaExpedicionFactura>").append(record.getFacturaRectificadaFecha().format(FECHA_FORMAT)).append("</sf:FechaExpedicionFactura>");
                xml.append("</sf:IDFacturaRectificada></sf:FacturasRectificadas>");
            }
        }

        xml.append("<sf:DescripcionOperacion>").append(escapeXml(record.getDescripcionOperacion())).append("</sf:DescripcionOperacion>");

        // Desglose
        xml.append("<sf:Desglose>");
        xml.append("<sf:DetalleDesglose>");
        xml.append("<sf:Impuesto>01</sf:Impuesto>");
        xml.append("<sf:ClaveRegimen>").append(record.getClaveRegimen()).append("</sf:ClaveRegimen>");
        xml.append("<sf:CalificacionOperacion>S1</sf:CalificacionOperacion>");
        xml.append("<sf:TipoImpositivo>").append(String.format("%.2f", (double) record.getVatPercentage())).append("</sf:TipoImpositivo>");
        xml.append("<sf:BaseImponibleOimporteNoSujeto>").append(record.getBaseImponible().toPlainString()).append("</sf:BaseImponibleOimporteNoSujeto>");
        xml.append("<sf:CuotaRepercutida>").append(record.getCuotaRepercutida().toPlainString()).append("</sf:CuotaRepercutida>");
        xml.append("</sf:DetalleDesglose>");
        xml.append("</sf:Desglose>");

        xml.append("<sf:CuotaTotal>").append(record.getCuotaTotal().toPlainString()).append("</sf:CuotaTotal>");
        xml.append("<sf:ImporteTotal>").append(record.getImporteTotal().toPlainString()).append("</sf:ImporteTotal>");

        // Encadenamiento
        xml.append("<sf:Encadenamiento>");
        if (record.getHuellaAnterior() == null) {
            xml.append("<sf:PrimerRegistro>S</sf:PrimerRegistro>");
        } else {
            xml.append("<sf:RegistroAnterior>");
            xml.append("<sf:Huella>").append(record.getHuellaAnterior()).append("</sf:Huella>");
            xml.append("</sf:RegistroAnterior>");
        }
        xml.append("</sf:Encadenamiento>");

        // SistemaInformatico
        xml.append("<sf:SistemaInformatico>");
        xml.append("<sf:NombreRazon>").append(escapeXml(productorNombre)).append("</sf:NombreRazon>");
        xml.append("<sf:NIF>").append(productorNif).append("</sf:NIF>");
        xml.append("<sf:NombreSistemaInformatico>Billaton</sf:NombreSistemaInformatico>");
        xml.append("<sf:IdSistemaInformatico>").append(sistemaId).append("</sf:IdSistemaInformatico>");
        xml.append("<sf:Version>").append(sistemaVersion).append("</sf:Version>");
        xml.append("<sf:NumeroInstalacion>BILLATON-001</sf:NumeroInstalacion>");
        xml.append("<sf:TipoUsoPosibleSoloVerifactu>S</sf:TipoUsoPosibleSoloVerifactu>");
        xml.append("<sf:TipoUsoPosibleMultiOT>N</sf:TipoUsoPosibleMultiOT>");
        xml.append("<sf:IndicadorMultiplesOT>N</sf:IndicadorMultiplesOT>");
        xml.append("</sf:SistemaInformatico>");

        xml.append("<sf:FechaHoraHusoGenRegistro>").append(fechaHora).append("</sf:FechaHoraHusoGenRegistro>");
        xml.append("<sf:TipoHuella>01</sf:TipoHuella>");
        xml.append("<sf:Huella>").append(record.getHuella()).append("</sf:Huella>");

        xml.append("</sf:RegistroAlta>");
        xml.append("</sum:RegistroFactura>");
        xml.append("</sum:RegFactuSistemaFacturacion>");
        xml.append("</soapenv:Body>");
        xml.append("</soapenv:Envelope>");

        return xml.toString();
    }

    public String buildAnulacionXml(VerifactuRecordDto record) {
        String fechaExp = record.getFechaExpedicion().format(FECHA_FORMAT);
        String fechaHora = record.getFechaHoraGenRegistro().format(FECHA_HORA_FORMAT);

        StringBuilder xml = new StringBuilder();
        xml.append("<soapenv:Envelope xmlns:soapenv=\"").append(NS_SOAP).append("\">");
        xml.append("<soapenv:Body>");
        xml.append("<sum:RegFactuSistemaFacturacion xmlns:sum=\"").append(NS_SUM).append("\" xmlns:sf=\"").append(NS_SF).append("\">");

        xml.append("<sum:Cabecera>");
        xml.append("<sf:ObligadoEmision>");
        xml.append("<sf:NombreRazon>").append(escapeXml(record.getIssuerName())).append("</sf:NombreRazon>");
        xml.append("<sf:NIF>").append(record.getIssuerNif()).append("</sf:NIF>");
        xml.append("</sf:ObligadoEmision>");
        xml.append("</sum:Cabecera>");

        xml.append("<sum:RegistroFactura>");
        xml.append("<sf:RegistroAnulacion>");
        xml.append("<sf:IDVersion>1.0</sf:IDVersion>");

        xml.append("<sf:IDFactura>");
        xml.append("<sf:IDEmisorFacturaAnulada>").append(record.getIssuerNif()).append("</sf:IDEmisorFacturaAnulada>");
        xml.append("<sf:NumSerieFacturaAnulada>").append(escapeXml(record.getNumSerieFactura())).append("</sf:NumSerieFacturaAnulada>");
        xml.append("<sf:FechaExpedicionFacturaAnulada>").append(fechaExp).append("</sf:FechaExpedicionFacturaAnulada>");
        xml.append("</sf:IDFactura>");

        xml.append("<sf:SistemaInformatico>");
        xml.append("<sf:NombreRazon>").append(escapeXml(productorNombre)).append("</sf:NombreRazon>");
        xml.append("<sf:NIF>").append(productorNif).append("</sf:NIF>");
        xml.append("<sf:NombreSistemaInformatico>Billaton</sf:NombreSistemaInformatico>");
        xml.append("<sf:IdSistemaInformatico>").append(sistemaId).append("</sf:IdSistemaInformatico>");
        xml.append("<sf:Version>").append(sistemaVersion).append("</sf:Version>");
        xml.append("<sf:NumeroInstalacion>BILLATON-001</sf:NumeroInstalacion>");
        xml.append("<sf:TipoUsoPosibleSoloVerifactu>S</sf:TipoUsoPosibleSoloVerifactu>");
        xml.append("<sf:TipoUsoPosibleMultiOT>N</sf:TipoUsoPosibleMultiOT>");
        xml.append("<sf:IndicadorMultiplesOT>N</sf:IndicadorMultiplesOT>");
        xml.append("</sf:SistemaInformatico>");

        xml.append("<sf:Encadenamiento>");
        if (record.getHuellaAnterior() == null) {
            xml.append("<sf:PrimerRegistro>S</sf:PrimerRegistro>");
        } else {
            xml.append("<sf:RegistroAnterior>");
            xml.append("<sf:Huella>").append(record.getHuellaAnterior()).append("</sf:Huella>");
            xml.append("</sf:RegistroAnterior>");
        }
        xml.append("</sf:Encadenamiento>");

        xml.append("<sf:FechaHoraHusoGenRegistro>").append(fechaHora).append("</sf:FechaHoraHusoGenRegistro>");
        xml.append("<sf:TipoHuella>01</sf:TipoHuella>");
        xml.append("<sf:Huella>").append(record.getHuella()).append("</sf:Huella>");

        xml.append("</sf:RegistroAnulacion>");
        xml.append("</sum:RegistroFactura>");
        xml.append("</sum:RegFactuSistemaFacturacion>");
        xml.append("</soapenv:Body>");
        xml.append("</soapenv:Envelope>");

        return xml.toString();
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
