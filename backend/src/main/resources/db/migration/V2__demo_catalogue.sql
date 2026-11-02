INSERT INTO categories(name,slug,description) VALUES
('Home & Living','home-living','Comfortable pieces for everyday spaces'),
('Accessories','accessories','Finishing touches and useful essentials'),
('Lifestyle','lifestyle','Thoughtful everyday favourites');
INSERT INTO products(category_id,name,description,price,stock,image_url) VALUES
((SELECT id FROM categories WHERE slug='home-living'),'Everyday Ceramic Vase','A sculptural ceramic vase for a shelf, table or bedside.',899.00,25,'https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=900'),
((SELECT id FROM categories WHERE slug='home-living'),'Soft Linen Cushion','A relaxed texture and a comfortable finish for your favourite corner.',1299.00,18,'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=900'),
((SELECT id FROM categories WHERE slug='accessories'),'Minimal Carry Tote','A dependable everyday tote with a clean, simple silhouette.',649.00,40,'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=900'),
((SELECT id FROM categories WHERE slug='accessories'),'Classic Wristwatch','An understated timepiece for daily wear.',2499.00,12,'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=900'),
((SELECT id FROM categories WHERE slug='lifestyle'),'Daily Notes Journal','A simple place for plans, ideas and little observations.',399.00,60,'https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=900'),
((SELECT id FROM categories WHERE slug='lifestyle'),'Reusable Travel Bottle','A reusable bottle for commutes, walks and weekend plans.',799.00,32,'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=900');
