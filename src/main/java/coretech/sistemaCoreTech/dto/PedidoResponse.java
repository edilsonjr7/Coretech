package coretech.sistemaCoreTech.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoResponse {

    private Long id;
    private String cliente;
    private String email;
    private LocalDateTime data;
    private BigDecimal total;
    private List<String> itensDescricao = new ArrayList<>();
    private String statusEmail;

    public PedidoResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<String> getItensDescricao() {
        return itensDescricao;
    }

    public void setItensDescricao(List<String> itensDescricao) {
        this.itensDescricao = itensDescricao;
    }

    public String getStatusEmail() {
        return statusEmail;
    }

    public void setStatusEmail(String statusEmail) {
        this.statusEmail = statusEmail;
    }
}
