package com.acc.connectauto.exception;

import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.exc.InvalidFormatException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException resourceNotFoundException) {
        log.warn("Recurso não encontrado: {}", resourceNotFoundException.getMessage());
        return respond(HttpStatus.NOT_FOUND, resourceNotFoundException.getMessage());
    }

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<ApiError> handleCepInvalido(CepInvalidoException cepInvalidoException) {
        log.warn("CEP inválido: {}", cepInvalidoException.getMessage());
        return respond(HttpStatus.BAD_REQUEST, cepInvalidoException.getMessage());
    }

    @ExceptionHandler(CepIndisponivelException.class)
    public ResponseEntity<ApiError> handleCepIndisponivel(CepIndisponivelException cepIndisponivelException) {
        log.warn("ViaCEP indisponível: {}", cepIndisponivelException.getMessage());
        return respond(HttpStatus.SERVICE_UNAVAILABLE, cepIndisponivelException.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException dataIntegrityViolationException) {
        log.warn("Violação de integridade de dados: {}",
                dataIntegrityViolationException.getMostSpecificCause().getMessage());
        return respond(HttpStatus.CONFLICT,
                "Operação viola uma restrição de integridade dos dados "
                        + "(ex.: valor único já cadastrado ou registro ainda referenciado por outro recurso).");
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiError> handleCredenciaisInvalidas(
            CredenciaisInvalidasException credenciaisInvalidasException) {
        log.warn(credenciaisInvalidasException.getMessage());
        return respond(HttpStatus.UNAUTHORIZED, credenciaisInvalidasException.getMessage());
    }

    @ExceptionHandler(DealerComVeiculosAssociadosException.class)
    public ResponseEntity<ApiError> handleDealerComVeiculosAssociados(
            DealerComVeiculosAssociadosException dealerComVeiculosAssociadosException) {
        log.warn(dealerComVeiculosAssociadosException.getMessage());
        return respond(HttpStatus.CONFLICT, dealerComVeiculosAssociadosException.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException httpMessageNotReadableException) {
        Throwable causaMaisEspecifica = httpMessageNotReadableException.getMostSpecificCause();
        log.warn("Corpo da requisição inválido: {}", causaMaisEspecifica.getMessage());

        return respond(HttpStatus.BAD_REQUEST, mensagemParaCorpoInvalido(causaMaisEspecifica));
    }

    private String mensagemParaCorpoInvalido(Throwable causaMaisEspecifica) {
        if (causaMaisEspecifica instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType() != null
                && invalidFormatException.getTargetType().isEnum()) {
            Object[] valoresAceitos = invalidFormatException.getTargetType().getEnumConstants();
            return "Valor '%s' inválido. Valores aceitos: %s"
                    .formatted(invalidFormatException.getValue(), Arrays.toString(valoresAceitos));
        }
        return "Corpo da requisição inválido: verifique os tipos e valores dos campos enviados.";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException methodArgumentNotValidException) {
        List<String> fieldErrorMessages = methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        log.warn("Erro de validação: {}", fieldErrorMessages);
        return respond(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrorMessages);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
        String message = "Parâmetro '%s' com valor inválido: '%s'".formatted(
                methodArgumentTypeMismatchException.getName(), methodArgumentTypeMismatchException.getValue());
        log.warn(message);
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException) {
        log.warn("Método HTTP não suportado: {}", httpRequestMethodNotSupportedException.getMessage());
        return respond(HttpStatus.METHOD_NOT_ALLOWED, httpRequestMethodNotSupportedException.getMessage());
    }

    private String formatFieldError(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        log.error("Erro inesperado não tratado", exception);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), status.getReasonPhrase(), message));
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), message, details));
    }
}
