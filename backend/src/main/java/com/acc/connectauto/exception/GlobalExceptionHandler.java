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

    // Diferente de CepInvalidoException: aqui a falha é do ViaCEP (rede/timeout/serviço
    // fora do ar), não do CEP em si — por isso 503, não 400.
    @ExceptionHandler(CepIndisponivelException.class)
    public ResponseEntity<ApiError> handleCepIndisponivel(CepIndisponivelException cepIndisponivelException) {
        log.warn("ViaCEP indisponível: {}", cepIndisponivelException.getMessage());
        return respond(HttpStatus.SERVICE_UNAVAILABLE, cepIndisponivelException.getMessage());
    }

    // Cobre violações de constraint do banco não antecipadas por uma checagem própria no
    // service (ex.: CNPJ ou chassi duplicado — o INSERT falha na constraint UNIQUE antes
    // de qualquer verificação prévia). Sem esse handler, cai no catch-all e vira 500 mesmo
    // sendo um erro do cliente. Casos onde dá pra checar antes (ex.: dealer com veículos
    // associados) usam checagem proativa em vez de depender só disso — ver
    // DealerService.excluir e DealerComVeiculosAssociadosException.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException dataIntegrityViolationException) {
        log.warn("Violação de integridade de dados: {}", dataIntegrityViolationException.getMostSpecificCause().getMessage());
        return respond(HttpStatus.CONFLICT,
                "Operação viola uma restrição de integridade dos dados "
                        + "(ex.: valor único já cadastrado ou registro ainda referenciado por outro recurso).");
    }

    @ExceptionHandler(DealerComVeiculosAssociadosException.class)
    public ResponseEntity<ApiError> handleDealerComVeiculosAssociados(
            DealerComVeiculosAssociadosException dealerComVeiculosAssociadosException) {
        log.warn(dealerComVeiculosAssociadosException.getMessage());
        return respond(HttpStatus.CONFLICT, dealerComVeiculosAssociadosException.getMessage());
    }

    // Cobre JSON malformado e valores fora do enum (ex.: "tipoCombustivel": "X") — o Jackson
    // rejeita o valor antes mesmo do @Valid entrar em ação, então precisa de handler próprio
    // pra não cair no tratamento de erro padrão do Spring (sem o formato ApiError). A
    // mensagem original do Jackson fica só no log — o cliente recebe uma mensagem própria
    // pra não vazar nome de classe/pacote interno da aplicação.
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

    // Ex.: GET /vehicles/abc, onde {vehicleId} espera um Long. Sem esse handler, o
    // catch-all intercepta antes do resolvedor padrão do Spring e devolve 500 em vez do
    // 400 que o Spring já daria de graça.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
        String message = "Parâmetro '%s' com valor inválido: '%s'".formatted(
                methodArgumentTypeMismatchException.getName(), methodArgumentTypeMismatchException.getValue());
        log.warn(message);
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    // Ex.: PATCH /dealer/1, verbo não suportado nesse endpoint. Mesmo motivo do handler
    // acima: preservar o 405 que o Spring já resolveria sozinho sem o catch-all.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException) {
        log.warn("Método HTTP não suportado: {}", httpRequestMethodNotSupportedException.getMessage());
        return respond(HttpStatus.METHOD_NOT_ALLOWED, httpRequestMethodNotSupportedException.getMessage());
    }

    private String formatFieldError(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }

    // Catch-all: qualquer exceção não mapeada pelos handlers acima. A stack trace e a
    // mensagem original vão só pro log do servidor — o cliente recebe uma mensagem
    // genérica, nunca detalhes internos da aplicação. Fica por último na leitura do
    // arquivo só por convenção; o Spring já escolhe o handler mais específico disponível
    // para cada exceção, independente da ordem de declaração.
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
