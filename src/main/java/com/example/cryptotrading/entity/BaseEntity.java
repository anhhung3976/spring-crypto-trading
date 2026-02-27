package com.example.cryptotrading.entity;

import com.example.cryptotrading.util.UserUtil;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    public static final String SYSTEM_AUDIT_USER = "system-auto";

    @Column(name = "ctl_act")
    private Boolean ctlAct = true;

    @Column(name = "ctl_tcn", precision = 5)
    @Version
    private Short ctlTcn;

    @Column(name = "ctl_cre_ts")
    private LocalDateTime ctlCreTs;

    @Column(name = "ctl_mod_ts")
    private LocalDateTime ctlModTs;

    @Column(name = "ctl_cre_uid", length = 32)
    private String ctlCreUid;

    @Column(name = "ctl_mod_uid", length = 32)
    private String ctlModUid;

    private static String getAuditUserLogin() {
        try {
            String login = UserUtil.getCurrentLoginUser();
            return (login != null) ? login : SYSTEM_AUDIT_USER;
        } catch (Exception ex) {
            return SYSTEM_AUDIT_USER;
        }
    }

    @PrePersist
    void prePersist() {
        ctlCreTs = now();
        ctlCreUid = ctlCreUid != null ? ctlCreUid : getAuditUserLogin();
    }

    @PreUpdate
    void preUpdate() {
        ctlModTs = now();
        ctlModUid = getAuditUserLogin();
    }

    public void manuallyUpdateAuditInfo() {
        preUpdate();
    }

    private static LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getId());
    }

    public abstract Long getId();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + getId() +
                '}';
    }
}
