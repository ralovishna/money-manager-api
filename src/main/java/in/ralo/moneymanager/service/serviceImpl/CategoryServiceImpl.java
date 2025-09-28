package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.CategoryDTO;
import in.ralo.moneymanager.model.Category;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.CategoryRepo;
import in.ralo.moneymanager.service.CategoryService;
import in.ralo.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ProfileService profileService;
    private final CategoryRepo categoryRepo;

    //save category after checking if it exists for a user
    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        Profile existingProfile = profileService.getCurrentProfile();

        if (categoryRepo.existsByNameAndProfileId(categoryDTO.getName(), existingProfile.getId()))
            throw new RuntimeException("Category with this name already exists.");

        Category newCategory = toEntity(categoryDTO, existingProfile);

        return toDto(categoryRepo.save(newCategory));
    }

    //get all categories for current user
    @Override
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        List<Category> categories = categoryRepo.findByProfileId(currentUser.getId());

        return categories.stream().map(this::toDto).toList();
    }

    //get all categories by type for current user
    @Override
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String categoryType) {
        Profile currentUser = profileService.getCurrentProfile();
        List<Category> categories = categoryRepo.findByTypeAndProfileId(categoryType, currentUser.getId());

        return categories.stream().map(this::toDto).toList();
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Profile currentUser = profileService.getCurrentProfile();
        Optional<Category> optionalCategory = Optional.ofNullable(categoryRepo.findByIdAndProfileId(categoryId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Category not found with category id " + categoryId)));

        Category existingCategory = optionalCategory.get();
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());

        return toDto(categoryRepo.save(existingCategory));
    }

    //get categories by profile id


    //helper methods
    private Category toEntity(CategoryDTO categoryDTO, Profile profile) {
        return Category.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDto(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .profileId(category.getProfile() != null ? category.getProfile().getId() : null)
                .type(category.getType())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}