package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.tp.application.port.in.CreateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.DeleteThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyStatisticsUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.LookupThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyBankAccountsUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyLifecycleUseCase;
import yowyob.comops.api.tp.application.port.in.SearchThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyUseCase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController extends CommercialThirdPartyController {

    public CustomerController(CreateThirdPartyUseCase createThirdPartyUseCase,
            GetThirdPartyUseCase getThirdPartyUseCase,
            ListThirdPartiesUseCase listThirdPartiesUseCase,
            SearchThirdPartiesUseCase searchThirdPartiesUseCase,
            UpdateThirdPartyUseCase updateThirdPartyUseCase,
            DeleteThirdPartyUseCase deleteThirdPartyUseCase,
            ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase,
            ManageThirdPartyBankAccountsUseCase manageThirdPartyBankAccountsUseCase,
            LookupThirdPartyUseCase lookupThirdPartyUseCase,
            GetThirdPartyStatisticsUseCase getThirdPartyStatisticsUseCase) {
        super(createThirdPartyUseCase, getThirdPartyUseCase, listThirdPartiesUseCase, searchThirdPartiesUseCase,
                updateThirdPartyUseCase, deleteThirdPartyUseCase, manageThirdPartyLifecycleUseCase,
                manageThirdPartyBankAccountsUseCase, lookupThirdPartyUseCase, getThirdPartyStatisticsUseCase,
                "CUSTOMER", "Customer", false);
    }
}
