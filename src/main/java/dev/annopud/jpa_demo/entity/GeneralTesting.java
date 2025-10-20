package dev.annopud.jpa_demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Entity
@Getter
@Setter
//@ToString
//@SuperBuilder
@NoArgsConstructor
@Table(name = "general_testing")
@Slf4j
public class GeneralTesting {

    /**
     * Case ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "email_from", nullable = false)
    private String emailFrom;

    @Column(name = "email_to", nullable = false)
    private String emailTo;

    @Column(name = "email_cc")
    private String emailCc;

    @Column(name = "email_bcc")
    private String emailBcc;

//    @Enumerated(EnumType.STRING)
//    @Convert(converter = EmailStatusConverter.class)
    @Column(name = "email_status")
    private EmailStatus emailStatus;

    @Column(name = "email_date")
    private Date emailDate;

    @Column(name = "create_by", nullable = false)
    private String createBy;

    @Column(name = "create_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name = "event_timestamp", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
//    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    2025-10-08T00:59:50
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "Asia/Bangkok")
    private Date createDate;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "email_submit_param")
    private byte[] emailSubmitParam;

    @Column(name = "email_error")
    private String emailError;

}
