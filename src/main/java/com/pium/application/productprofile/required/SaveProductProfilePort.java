package com.pium.application.productprofile.required;

import com.pium.domain.productprofile.model.ProductProfile;

/**
 * ProductProfile을 저장하기 위한 포트.
 */
public interface SaveProductProfilePort {

    ProductProfile save(ProductProfile productProfile);
}
