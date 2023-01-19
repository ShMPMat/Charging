package io.tashtabash.charging.entity;


import javax.persistence.*;
import java.util.Objects;


@Entity
public class Company {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private Company parentCompany;

    public Company() {}

    public Company(String name, Company parentCompany) {
        this.name = name;
        this.parentCompany = parentCompany;
    }

    public Company(long id, String name, Company parentCompany) {
        this.id = id;
        this.name = name;
        this.parentCompany = parentCompany;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Company getParentCompany() {
        return parentCompany;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentCompany(Company parentCompany) {
        this.parentCompany = parentCompany;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        if (id != company.id) return false;
        if (!Objects.equals(name, company.name)) return false;
        return Objects.equals(parentCompany, company.parentCompany);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parentCompany != null ? (int) parentCompany.id : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentCompany=" + parentCompany +
                '}';
    }
}
