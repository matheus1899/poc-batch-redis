package com.tenorinho.poc_batch_redis.model;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "paises")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paises {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  @Column(name = "nome", length = 50)
  private String nome;
}
