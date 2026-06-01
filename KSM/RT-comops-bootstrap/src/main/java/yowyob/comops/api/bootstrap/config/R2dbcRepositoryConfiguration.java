package yowyob.comops.api.bootstrap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@Profile("r2dbc")
@EnableR2dbcRepositories(basePackages = {
        "yowyob.comops.api.common.adapter.out.persistence",
        "yowyob.comops.api.actor.adapter.out.persistence",
        "yowyob.comops.api.auth.adapter.out.persistence",
        "yowyob.comops.api.roles.adapter.out.persistence",
        "yowyob.comops.api.organization.adapter.out.persistence",
        "yowyob.comops.api.tp.adapter.out.persistence",
        "yowyob.comops.api.product.adapter.out.persistence",
        "yowyob.comops.api.inventory.adapter.out.persistence",
        "yowyob.comops.api.resource.adapter.out.persistence",
        "yowyob.comops.api.settings.adapter.out.persistence",
        "yowyob.comops.api.sales.adapter.out.persistence",
        "yowyob.comops.api.billing.persistence",
        "yowyob.comops.api.accounting.adapter.out.persistence",
        "yowyob.comops.api.accounting.extension.persistence",
        "yowyob.comops.api.treasury.adapter.out.persistence",
        "yowyob.comops.api.cashier.persistence",
        "yowyob.comops.api.administration.adapter.out.persistence",
        "yowyob.comops.api.file.adapter.out.persistence",
        "yowyob.comops.api.hrm.adapter.out.persistence",
        "yowyob.comops.api.blockchain.adapter.out.persistence",
        "yowyob.comops.api.kernel.adapter.out.persistence"
})
public class R2dbcRepositoryConfiguration {
}
