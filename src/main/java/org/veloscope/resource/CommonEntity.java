package org.veloscope.resource;

import javax.persistence.*;

@MappedSuperclass
public class CommonEntity implements EntityInterface {
    protected Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
