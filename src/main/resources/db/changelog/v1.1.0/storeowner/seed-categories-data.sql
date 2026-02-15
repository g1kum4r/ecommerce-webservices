-- Seed store categories
INSERT INTO store_categories (name, slug, description, parent_id, level) VALUES
('Electronics', 'electronics', 'Electronic devices and gadgets', NULL, 0),
('Fashion', 'fashion', 'Clothing and accessories', NULL, 0),
('Home & Garden', 'home-garden', 'Home improvement and garden supplies', NULL, 0),
('Toys & Games', 'toys-games', 'Toys and gaming products', NULL, 0),
('Sports & Outdoors', 'sports-outdoors', 'Sports equipment and outdoor gear', NULL, 0),
('Books & Media', 'books-media', 'Books, movies, and music', NULL, 0),
('Health & Beauty', 'health-beauty', 'Health and beauty products', NULL, 0),
('Food & Beverage', 'food-beverage', 'Food and drink products', NULL, 0),
('Automotive', 'automotive', 'Auto parts and accessories', NULL, 0),
('Pet Supplies', 'pet-supplies', 'Pet food and accessories', NULL, 0);

-- Seed products categories (with hierarchy)
-- Level 0 (Root categories)
INSERT INTO products_categories (name, slug, description, parent_id, level) VALUES
('Electronics', 'electronics', 'Electronic devices and accessories', NULL, 0),
('Computers & Tablets', 'computers-tablets', 'Computers, laptops, and tablets', NULL, 0),
('Mobile Phones', 'mobile-phones', 'Smartphones and mobile devices', NULL, 0),
('Cameras & Photography', 'cameras-photography', 'Cameras and photography equipment', NULL, 0),
('Audio & Video', 'audio-video', 'Audio and video equipment', NULL, 0),
('Home Appliances', 'home-appliances', 'Home and kitchen appliances', NULL, 0),
('Fashion & Clothing', 'fashion-clothing', 'Clothing and fashion accessories', NULL, 0),
('Shoes & Footwear', 'shoes-footwear', 'Shoes and footwear for all', NULL, 0),
('Jewelry & Watches', 'jewelry-watches', 'Jewelry and watches', NULL, 0),
('Bags & Luggage', 'bags-luggage', 'Bags and travel luggage', NULL, 0),
('Sports Equipment', 'sports-equipment', 'Sports and fitness equipment', NULL, 0),
('Outdoor & Camping', 'outdoor-camping', 'Outdoor and camping gear', NULL, 0),
('Toys & Games', 'toys-games', 'Toys and games for all ages', NULL, 0),
('Books', 'books', 'Books and reading materials', NULL, 0),
('Health & Personal Care', 'health-personal-care', 'Health and personal care products', NULL, 0),
('Beauty & Cosmetics', 'beauty-cosmetics', 'Beauty and cosmetic products', NULL, 0),
('Home & Kitchen', 'home-kitchen', 'Home and kitchen essentials', NULL, 0),
('Furniture', 'furniture', 'Home and office furniture', NULL, 0),
('Baby & Kids', 'baby-kids', 'Baby and kids products', NULL, 0),
('Automotive', 'automotive', 'Automotive parts and accessories', NULL, 0),
('Pet Supplies', 'pet-supplies', 'Pet food and accessories', NULL, 0),
('Office Products', 'office-products', 'Office supplies and equipment', NULL, 0),
('Grocery & Food', 'grocery-food', 'Grocery and food items', NULL, 0),
('Garden & Outdoor', 'garden-outdoor', 'Garden and outdoor supplies', NULL, 0);

-- Level 1 (Subcategories for Electronics)
INSERT INTO products_categories (name, slug, description, parent_id, level) VALUES
('Smartphones', 'smartphones', 'Latest smartphones', (SELECT id FROM products_categories WHERE slug = 'mobile-phones'), 1),
('Feature Phones', 'feature-phones', 'Basic mobile phones', (SELECT id FROM products_categories WHERE slug = 'mobile-phones'), 1),
('Phone Accessories', 'phone-accessories', 'Mobile phone accessories', (SELECT id FROM products_categories WHERE slug = 'mobile-phones'), 1),
('Laptops', 'laptops', 'Laptops and notebooks', (SELECT id FROM products_categories WHERE slug = 'computers-tablets'), 1),
('Desktop Computers', 'desktop-computers', 'Desktop PCs', (SELECT id FROM products_categories WHERE slug = 'computers-tablets'), 1),
('Tablets', 'tablets', 'Tablet devices', (SELECT id FROM products_categories WHERE slug = 'computers-tablets'), 1),
('Computer Accessories', 'computer-accessories', 'Computer peripherals and accessories', (SELECT id FROM products_categories WHERE slug = 'computers-tablets'), 1),
('Headphones', 'headphones', 'Headphones and earphones', (SELECT id FROM products_categories WHERE slug = 'audio-video'), 1),
('Speakers', 'speakers', 'Audio speakers', (SELECT id FROM products_categories WHERE slug = 'audio-video'), 1),
('Home Theater', 'home-theater', 'Home theater systems', (SELECT id FROM products_categories WHERE slug = 'audio-video'), 1),
('DSLR Cameras', 'dslr-cameras', 'DSLR and mirrorless cameras', (SELECT id FROM products_categories WHERE slug = 'cameras-photography'), 1),
('Digital Cameras', 'digital-cameras', 'Point and shoot cameras', (SELECT id FROM products_categories WHERE slug = 'cameras-photography'), 1),
('Camera Lenses', 'camera-lenses', 'Camera lenses and accessories', (SELECT id FROM products_categories WHERE slug = 'cameras-photography'), 1);

-- Level 1 (Subcategories for Fashion)
INSERT INTO products_categories (name, slug, description, parent_id, level) VALUES
('Men''s Clothing', 'mens-clothing', 'Clothing for men', (SELECT id FROM products_categories WHERE slug = 'fashion-clothing'), 1),
('Women''s Clothing', 'womens-clothing', 'Clothing for women', (SELECT id FROM products_categories WHERE slug = 'fashion-clothing'), 1),
('Kids'' Clothing', 'kids-clothing', 'Clothing for children', (SELECT id FROM products_categories WHERE slug = 'fashion-clothing'), 1),
('Men''s Shoes', 'mens-shoes', 'Footwear for men', (SELECT id FROM products_categories WHERE slug = 'shoes-footwear'), 1),
('Women''s Shoes', 'womens-shoes', 'Footwear for women', (SELECT id FROM products_categories WHERE slug = 'shoes-footwear'), 1),
('Kids'' Shoes', 'kids-shoes', 'Footwear for children', (SELECT id FROM products_categories WHERE slug = 'shoes-footwear'), 1);
