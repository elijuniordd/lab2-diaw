package br.pucminas.aluguelcarros.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Crédito concedido por um banco e associado a um contrato de aluguel. */
@Entity
@Table(name = "contratos_credito")
@Getter
@Setter
@NoArgsConstructor
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private Contrato contrato;

    /** Usuário AGENTE do tipo BANCO que concedeu o crédito. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Usuario banco;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Taxa de juros ao mês, em percentual (ex.: 1.99). */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal juros;
}
